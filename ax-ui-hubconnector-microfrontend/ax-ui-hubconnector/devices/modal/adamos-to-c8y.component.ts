import { Component, EventEmitter, Output } from "@angular/core";
import { IIdentified, InventoryService } from "@c8y/client";
import { GroupNodeService } from "@c8y/ngx-components/assets-navigator";
import { UnlinkedDevicesNode } from "./unlinked-devices-group-node";

@Component({
  selector: "adamos-to-c8y-form",
  template: `<div class="form-group">
    <c8y-asset-selector
      [(ngModel)]="selectedC8yDevice"
      (onSelected)="selectionChanged($event)"
      [config]="{
        label: 'Unlinked Cumulocity IoT Devices' | translate,
        groupsSelectable: false,
        showChildDevices: false,
        showUnassignedDevices: false,
        showFilter: true,
        search: true
      }"
      [rootNode]="unlinkedDevicesNode"
    ></c8y-asset-selector>
  </div>`,
})
export class AdamosToCumulocityComponent {
  @Output() selectedDevice = new EventEmitter<number>();


  protected unlinkedDevicesNode: UnlinkedDevicesNode;
  protected selectedC8yDevice: IIdentified;

  constructor(groupNodeService: GroupNodeService, inventory: InventoryService) {
    this.unlinkedDevicesNode = new UnlinkedDevicesNode(
      groupNodeService,
      { showChildDevices: false },
      inventory
    );
  }

  selectionChanged(event: any): void {
    this.selectedDevice.emit(event.items.id);
  }
}
