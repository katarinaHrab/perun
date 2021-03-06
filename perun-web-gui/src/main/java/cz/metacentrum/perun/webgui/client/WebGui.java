package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.authzResolver.GetPerunPrincipal;
import cz.metacentrum.perun.webgui.json.authzResolver.KeepAlive;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.PerunPrincipal;
import cz.metacentrum.perun.webgui.tabs.TabManager;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilitiesSelectTabItem;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupsTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.SelfDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VosSelectTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.NotUserOfPerunWidget;

/**
 * The main web GUI class. It's GWT Entry point.
 *
 * Loads whole GUI, makes login to RPC by calling GetPerunPrincipal
 * Handles all changes in URL by default
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class WebGui implements EntryPoint, ValueChangeHandler<String> {

    // web session
    private PerunWebSession session = PerunWebSession.getInstance();
    // url mapper
    private UrlMapper urlMapper;

    // min client height & width
    static final public int MIN_CLIENT_HEIGHT = 600;  // when changed update .css file too !!
    static final public int MIN_CLIENT_WIDTH = 980; // when changed update .css file too !!

    // perun loaded (is checked when page loads)
    private boolean perunLoaded = false;

    private static boolean checkPending = false;

    /**
     * This is ENTRY POINT method. It's called automatically when web page
     * containing this GUI is loaded in browser.
     */
    public void onModuleLoad() {

        // Get web page's BODY
        RootLayoutPanel body = RootLayoutPanel.get();
        body.setStyleName("mainPanel");

        // check RPC url
        if(session.getRpcUrl().isEmpty()){
            VerticalPanel bodyContents = new VerticalPanel();
            bodyContents.setSize("100%", "300px");
            bodyContents.add(new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>RPC SERVER NOT FOUND!</h2>"));
            bodyContents.setCellHorizontalAlignment(bodyContents.getWidget(0), HasHorizontalAlignment.ALIGN_CENTER);
            bodyContents.setCellVerticalAlignment(bodyContents.getWidget(0), HasVerticalAlignment.ALIGN_BOTTOM);
            body.add(bodyContents);
            return;
        }

        // WEB PAGE SPLITTER
        DockLayoutPanel bodySplitter = new DockLayoutPanel(Unit.PX);
        body.add(bodySplitter);

        // MAIN CONTENT WRAPPER - make content resize-able
        ResizeLayoutPanel contentWrapper = new ResizeLayoutPanel();
        contentWrapper.setSize("100%", "100%");

        // MAIN CONTENT
        AbsolutePanel contentPanel = new AbsolutePanel();
        contentPanel.setSize("100%", "100%");
        // put content into wrapper
        contentWrapper.setWidget(contentPanel);

        // SETUP SESSION

        // store handler for main contetn's elements (tabs etc.) into session
        session.setUiElements(new UiElements(contentPanel));
        // Store TabManager into session for handling tabs (add/remove/close...)
        session.setTabManager(new TabManager());
        // Store this class into session
        session.setWebGui(this);

        // Set this class as browser's History handler
        History.addValueChangeHandler(this);
        // Sets URL mapper for loading proper tabs
        urlMapper = new UrlMapper();

        // MENU WRAPPER
        VerticalPanel menuWrapper = new VerticalPanel();
        menuWrapper.setHeight("100%");
        // add menu
        menuWrapper.add(session.getUiElements().getMenu().getWidget());
        menuWrapper.setCellVerticalAlignment(session.getUiElements().getMenu().getWidget(), HasVerticalAlignment.ALIGN_TOP);

        // Put all panels into web page splitter
        //	bodySplitter.addNorth(session.getUiElements().getHeader(), 78);
        bodySplitter.addNorth(session.getUiElements().getHeader(), 64);
        bodySplitter.addSouth(session.getUiElements().getFooter(), 30);
        bodySplitter.addWest(menuWrapper, 202);
        bodySplitter.add(contentWrapper); // content must be added as last !!

        // Append more GUI elements from UiElements class which are not part of splitted design
        if ("true".equalsIgnoreCase(Location.getParameter("log"))) {
            bodySplitter.getElement().appendChild(session.getUiElements().getLog().getElement()); // log
        }
        bodySplitter.getElement().appendChild(session.getUiElements().getStatus().getElement()); // status

        // LOGs current user to RPC
        loadPerunPrincipal();

        // keep alive
        final PopupPanel box = new DecoratedPopupPanel();
        box.setGlassEnabled(true);

        VerticalPanel vp = new VerticalPanel();
        vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        vp.add(new HTML("<h2>Connection to Perun lost.</h2><h2>Connecting...</h2>"));
        vp.add(new Image(AjaxLoaderImage.IMAGE_URL));
        vp.setSpacing(10);

        box.add(vp);
        box.setModal(true);
        box.center();
        box.hide();

        // Check RPC URL every 15 sec if call not pending
        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {

                if (!checkPending) {
                    KeepAlive call = new KeepAlive(new JsonCallbackEvents(){
                        @Override
                        public void onLoadingStart(){
                            checkPending = true;
                        }
                        @Override
                        public void onFinished(JavaScriptObject jso){
                            checkPending = false;
                            BasicOverlayType type = jso.cast();
                            if (type.getString().equals("OK")) {
                                box.hide();
                            }
                        }
                        @Override
                        public void onError(PerunError error){
                            checkPending = false;
                            if (!box.isShowing()) {
                                box.show();
                            }
                        }
                    });
                    call.retrieveData();
                }
                return true;
            }
        }, 15000);

    }

    /**
     * Performs a login into the RPC, loads user and his roles into session and enables GUI.
     */
    private void loadPerunPrincipal() {

        // show loading box
        final PopupPanel loadingBox = session.getUiElements().perunLoadingBox();
        loadingBox.show();

        // events after getting PerunPrincipal from RPC
        final JsonCallbackEvents events = new JsonCallbackEvents() {
            public void onFinished(JavaScriptObject jso) {

                // store perun principal into session for future use
                PerunPrincipal pp = (PerunPrincipal)jso;
                session.setPerunPrincipal(pp);

                // check if user exists
                if (session.getUser() == null && !pp.getRoles().hasAnyRole()) {
                    // if not and no role, redraw page body
                    RootLayoutPanel body = RootLayoutPanel.get();
                    loadingBox.hide();
                    body.clear();
                    body.add(new NotUserOfPerunWidget());
                    return;
                }

                // store users roles and editable entities into session
                session.setRoles(pp.getRoles());

                // display logged user
                session.getUiElements().setLoggedUserInfo(pp);
                session.getUiElements().setLogText("Welcome "+pp.getUser().getFullNameWithTitles());

                // show extended info ?
                boolean showExtendedInfo = false;

                // is perun admin ?
                if (session.isPerunAdmin()) {
                    showExtendedInfo = true;
                }

                // replace by local storage if possible
                Storage localStorage = Storage.getLocalStorageIfSupported();
                if (localStorage != null) {
                    String value = localStorage.getItem("urn:perun:gui:preferences:extendedInfo");
                    if (value != null) {
                        showExtendedInfo = Boolean.parseBoolean(value);
                    }
                }

                // finally set it
                JsonUtils.setExtendedInfoVisible(showExtendedInfo);
                session.getUiElements().getExtendedInfoButtonWidget().setDown(showExtendedInfo); // set extended info button

                // perun loaded = true
                perunLoaded = true;

                // loads the page based on URL or default
                loadPage();

                // hides the loading box
                loadingBox.hide();

                // load proper parts of menu (based on roles)
                session.getUiElements().getMenu().prepare();

            }

            public void onError(PerunError error){
                // hides the loading box
                loadingBox.hide();

                // shows error box
                PopupPanel loadingFailedBox;
                if (error == null) {
                    loadingFailedBox = session.getUiElements().perunLoadingFailedBox("Request timeout exceeded.");
                } else {
                    if (error.getName().contains("UserNotExistsException")) {
                        loadingFailedBox = session.getUiElements().perunLoadingFailedBox("You are not registered to any Virtual Organization.</br></br>" + error.getErrorInfo());
                    } else {
                        loadingFailedBox = session.getUiElements().perunLoadingFailedBox(error.getErrorInfo());
                    }
                }
                loadingFailedBox.show();
            }
        };
        GetPerunPrincipal loggedUserRequst = new GetPerunPrincipal(events);
        loggedUserRequst.retrieveData();
    }

    /**
     * Called automatically when page changes (tokens after #)
     */
    public void onValueChange(ValueChangeEvent<String> event) {
        changePage(History.getToken());
    }

    /**
     * Called when page is changed. Loads proper tabs base on tokens after #
     *
     * @param token Hash (value after #)
     */
    public void changePage(String token) {

        // if Perun not ready, user not authorized or in process of adding tabs
        if(!perunLoaded || session.getTabManager().isChangePageEventLocked()) {
            return;
        }

        urlMapper.parseUrl(token);

    }

    /**
     * Loads the page. Must be called after authentication process!
     */
    private void loadPage() {

        // when there is no token, the the default tabs are loaded
        // this is useful if a user has bookmarked a site other than the homepage.
        if(History.getToken().isEmpty()){

            // get whole URL
            String url = Window.Location.getHref();
            String newToken = "";

            int index = -1;

            if (url.contains("?gwt.codesvr=127.0.0.1:9997")) {
                // if local devel build

                if (url.contains("?locale=")) {
                    // with locale
                    index = url.indexOf("?", url.indexOf("?", url.indexOf("?"))+1);
                } else {
                    // without locale
                    index = url.indexOf("?", url.indexOf("?")+1);
                }
            } else {
                // if production build
                if (url.contains("?locale=")) {
                    // with locale
                    index = url.indexOf("?", url.indexOf("?")+1);
                } else {
                    // without locale
                    index = url.indexOf("?");
                }
            }

            if (index !=  -1) {
                newToken = url.substring(index+1);
            }

            // will sort of break URL, but will work without refreshing whole GUI
            if (newToken.isEmpty()) {
                // token was empty anyway - load default
                loadDefaultTabs();
            } else {
                // token is now correct - load it
                History.newItem(newToken);
            }

        } else {
            changePage(History.getToken());
        }

    }

    /**
     * Loads default tabs based on user's roles (called when no other tabs opened).
     * Must be called after authentication process !
     */
    private void loadDefaultTabs() {

        // perun / vo admin
        if(session.isPerunAdmin()) {
            // let perun admin decide what to open
            //session.getTabManager().addTab(new VosTabItem(session), true);
            return;
        }
        if(session.isVoAdmin()) {
            if (session.getEditableVos().size() > 1) {
                session.getTabManager().addTab(new VosSelectTabItem(), true);
            } else {
                session.getTabManager().addTab(new VoDetailTabItem(session.getEditableVos().get(0)), true);
            }
            return;
        }
        // facility admin
        if(session.isFacilityAdmin()){
            if (session.getEditableFacilities().size() > 1) {
                session.getTabManager().addTab(new FacilitiesSelectTabItem(), true);
            } else {
                session.getTabManager().addTab(new FacilityDetailTabItem(session.getEditableFacilities().get(0)), true);
            }
            return;
        }
        // group admin
        if(session.isGroupAdmin()){
            if (session.getEditableGroups().size() > 1) {
                session.getTabManager().addTab(new GroupsTabItem(null), true);
            } else {
                session.getTabManager().addTab(new GroupDetailTabItem(session.getEditableGroups().get(0)), true);
            }
            return;
        }

        // only user
        if(session.isSelf() && session.getUser() != null)
        {
            session.getTabManager().addTab(new SelfDetailTabItem(session.getUser()), true);
        }

    }

}