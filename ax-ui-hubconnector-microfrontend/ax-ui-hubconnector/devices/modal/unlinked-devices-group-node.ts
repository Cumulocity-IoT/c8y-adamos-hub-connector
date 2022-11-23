import { IManagedObject, InventoryService } from "@c8y/client";
import { ClickOptions, gettext } from "@c8y/ngx-components";
import {
  Action,
  GroupNode,
  GroupNodeConfig,
  GroupNodeService,
} from "@c8y/ngx-components/assets-navigator";

export class UnlinkedDevicesNode extends GroupNode {
  static NAME = "UnlinkedDevicesNode";
  label = gettext("Unlinked devices");
  icon = "delete-folder";
  groupsSelectable = false;
  groupsOnly = false;
  showChildDevices = false;
  mo = {};
  PAGE_SIZE = 20;

  constructor(
    protected service: GroupNodeService,
    config: GroupNodeConfig = {},
    private inventory: InventoryService
  ) {
    super(service, config);
    this.priority = Infinity;
    this.showChildDevices = config.showChildDevices || false;
  }

  click(options: ClickOptions = {}) {
    this.hookEvents();
    if (options.open) {
      this.events.next(Action.FETCH);
    }
  }

  addManagedObject(mo: IManagedObject) {
    this.add(
      this.service.createChildNode({
        mo,
        showChildDevices: this.showChildDevices,
      })
    );
  }

  fetch() {
    return this.getUnlinkedDevices(
      this.showChildDevices,
      this.filterQuery$.value
    );
  }

  private getUnlinkedDevices(withChildren = false, filterQuery = "") {
    const queryFilter: any = {
      fragmentType: "c8y_IsDevice",
      withChildren,
      pageSize: this.PAGE_SIZE,
      q: this.getUnassignedDevicesQueryStr(filterQuery),
    };
    return this.inventory.list(this.createFilter(queryFilter));
  }

  private getUnassignedDevicesQueryStr(filterQuery: any): string {
    const hasGroupId = filterQuery.includes("bygroupid");
    // Fetch all unassigned devices.
    const defaultQueryStr = "$filter=not has(adamos_hub_isDevice) $orderby=name";

    // filterQuery is a custom query to fetch unassigned devices filtered by name.
    return hasGroupId || !filterQuery ? defaultQueryStr : filterQuery;
  }

  private createFilter(extraParams: any = {}) {
    const params = {
      currentPage: 1,
      withTotalPages: true,
      pageSize: 10,
    };
    return { ...params, ...extraParams };
  }

  isGroup() {
    return true;
  }

  toString() {
    return UnlinkedDevicesNode.NAME;
  }
}
