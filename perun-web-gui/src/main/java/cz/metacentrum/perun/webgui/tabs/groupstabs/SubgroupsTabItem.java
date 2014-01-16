package cz.metacentrum.perun.webgui.tabs.groupstabs;

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
import cz.metacentrum.perun.webgui.json.groupsManager.GetSubGroups;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.tabs.GroupsTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Group admins page for Group Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class SubgroupsTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading subgroups");

	/**
	 * Group
	 */
	private Group group;
	private int groupId;


	/**
	 * Creates a tab instance
     * @param group
     */
	public SubgroupsTabItem(Group group){
		this.group = group;
		this.groupId = group.getId();
	}
	
	/**
	 * Creates a tab instance
     * @param groupId
     */
	public SubgroupsTabItem(int groupId){
		this.groupId = groupId;
        JsonCallbackEvents events = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                group = jso.cast();
            }
        };
        new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}
	
	
	public boolean isPrepared(){
		return !(group == null);
	}
	

	public Widget draw() {
		
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(group.getName()) + ": subgroups");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// if members group, hide
		if(group.isCoreGroup()){
			vp.add(new HTML("<h2>Members group cannot have subgroups.</h2>"));
			this.contentWidget.setWidget(vp);
			return getWidget();
		}

		// GROUP TABLE with onclick
		final GetSubGroups subgroups = new GetSubGroups(groupId);

		// Events for reloading when group is created
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(subgroups);

		// menu
		TabMenu menu = new TabMenu();

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createSubGroup(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                // creates a new form
                session.getTabManager().addTabToCurrentTab(new CreateGroupTabItem(group));
            }
        }));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteSubGroup());
        removeButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                final ArrayList<Group> itemsToRemove = subgroups.getTableSelectedList();
                String text = "Following groups (including all sub-groups) will be deleted.";
                UiElements.showDeleteConfirm(itemsToRemove, text, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                        for (int i=0; i<itemsToRemove.size(); i++) {
                            DeleteGroup request;
                            if (i == itemsToRemove.size()-1) {
                                request = new DeleteGroup(JsonCallbackEvents.disableButtonEvents(removeButton, events));
                            } else {
                                request = new DeleteGroup(JsonCallbackEvents.disableButtonEvents(removeButton));
                            }
                            request.deleteGroup(itemsToRemove.get(i).getId());
                        }
                    }
                });

            }
        });
        menu.addWidget(removeButton);

		// filter box
		menu.addFilterWidget(new ExtendedSuggestBox(subgroups.getOracle()), new PerunSearchEvent() {
            public void searchFor(String text) {
                subgroups.filterTable(text);
            }
        }, ButtonTranslation.INSTANCE.filterGroup());

		// add menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<Group> table = subgroups.getTable(new FieldUpdater<Group, String>() {
			@Override
			public void update(int arg0, Group group, String arg2) {
				session.getTabManager().addTab(new GroupDetailTabItem(group.getId()));
			}
		});

        removeButton.setEnabled(false);
        JsonUtils.addTableManagedButton(subgroups, table, removeButton);

		// adds the table into the panel
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.groupGoIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupId + 546;
		return result;
	}

	/**
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		SubgroupsTabItem create = (SubgroupsTabItem) obj;

		if (groupId != create.groupId){
			return false;
		}

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
        session.getUiElements().getBreadcrumbs().setLocation(group, "Subgroups", getUrlWithParameters());
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId()) || session.isGroupAdmin(group.getId())) {
			return true; 
		} else {
			return false;
		}

	}
	
	public final static String URL = "subgps";
	
	public String getUrl()
	{
		return URL;
	}
	
	public String getUrlWithParameters()
	{
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}
	
	static public SubgroupsTabItem load(Map<String, String> parameters)
	{
		int gid = Integer.parseInt(parameters.get("id"));
		return new SubgroupsTabItem(gid);
	}

}