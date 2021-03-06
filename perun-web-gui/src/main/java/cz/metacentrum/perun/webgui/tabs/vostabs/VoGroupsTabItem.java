package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.DeleteGroup;
import cz.metacentrum.perun.webgui.json.groupsManager.GetAllGroups;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.tabs.groupstabs.CreateGroupTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * VO Groups page.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class VoGroupsTabItem implements TabItem, TabItemWithUrl{

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("Loading vo groups");

	// data
	private VirtualOrganization vo;
	private int voId;

	/**
	 * Creates a tab instance
	 *
     * @param vo
     */
	public VoGroupsTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}
	
	/**
	 * Creates a tab instance
	 *
     * @param voId
     */
	public VoGroupsTabItem(int voId){
		this.voId = voId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                vo = jso.cast();
            }
        };
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}
	
	public boolean isPrepared(){
		return !(vo == null);
	}

	public Widget draw() {
		

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": "+"groups");
		
		// MAIN PANEL
		VerticalPanel firstTabPanel = new VerticalPanel();
		firstTabPanel.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();

		// VO Groups request
		final GetAllGroups groups = new GetAllGroups(voId);
        final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(groups);

		// add new group button
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createGroup(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                session.getTabManager().addTabToCurrentTab(new CreateGroupTabItem(vo));
            }
        }));

		// delete selected groups button
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteGroup());
        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final ArrayList<Group> groupsToDelete = groups.getTableSelectedList();
                String text = "Following groups (including all sub-groups) will be deleted.";
                UiElements.showDeleteConfirm(groupsToDelete, text, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                        for (int i=0; i<groupsToDelete.size(); i++ ) {
                            DeleteGroup request;
                            if(i == groupsToDelete.size() - 1) {
                                request = new DeleteGroup(JsonCallbackEvents.disableButtonEvents(removeButton, events));
                            } else {
                                request = new DeleteGroup(JsonCallbackEvents.disableButtonEvents(removeButton));
                            }
                            request.deleteGroup(groupsToDelete.get(i).getId());
                        }
                    }
                });
            }
        });
        menu.addWidget(removeButton);

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(groups.getOracle()), new PerunSearchEvent() {
            public void searchFor(String text) {
                groups.filterTable(text);
            }
        }, ButtonTranslation.INSTANCE.filterGroup());
		
		// add a table with a onclick		
		CellTable<Group> table = groups.getTable(new FieldUpdater<Group, String>() {
			public void update(int index, Group group, String value) {
				session.getTabManager().addTab(new GroupDetailTabItem(group));
			}
		});

		// add a class to the table and wrap it into scroll panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");		

		// add menu and the table to the main panel
		firstTabPanel.add(menu);
		firstTabPanel.setCellHeight(menu, "30px");
		firstTabPanel.add(sp);

        removeButton.setEnabled(false);
        JsonUtils.addTableManagedButton(groups, table, removeButton);

		session.getUiElements().resizePerunTable(sp, 350, this);


		this.contentWidget.setWidget(firstTabPanel);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.groupIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 51;
		int result = 1;
		result = prime * result + voId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VoGroupsTabItem other = (VoGroupsTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}


	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(vo, "Groups", getUrlWithParameters());
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}


	public boolean isAuthorized() {
		
		if (session.isVoAdmin(voId)) {
			return true; 
		} else {
			return false;
		}

	}
	
	public final static String URL = "groups";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}
	
	static public VoGroupsTabItem load(Map<String, String> parameters)
	{
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoGroupsTabItem(voId);
	}

}