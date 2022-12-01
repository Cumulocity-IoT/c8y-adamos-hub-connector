import { Component, EventEmitter, OnInit, Output } from "@angular/core";
import { IManagedObject, InventoryService, IResultList } from "@c8y/client";

@Component({
  selector: "adamos-to-c8y-form",
  template: `<div class="form-group" *ngIf="isLoading">
      <i c8y-icon="spinner" class="fa fw fa-spinner fa-spin"></i>
      {{ "Importing Cumulocity IoT devices..." | translate }}
    </div>

    <div class="card">
      <div class="card-header">
        <h4 class="card-title">
          {{ "Unlinked Cumulocity IoT Devices" | translate }}
        </h4>
      </div>

      <c8y-list-group class="card-inner-scroll" style="max-height: 200px">
        <c8y-li *c8yFor="let device of devices; loadMore: 'show'">
          <c8y-li-checkbox
            (onSelect)="onSelect($event, device)"
            (click)="$event.stopPropagation()"
            [selected]="isChecked(device)"
          ></c8y-li-checkbox>
          <c8y-li-icon [icon]="'device'"></c8y-li-icon>
          {{ device.name || "-" }}
        </c8y-li>
      </c8y-list-group>
    </div>

    <!-- <c8y-ui-empty-state
      *ngIf="!isLoading && devices.length === 0"
      [icon]="'c8y-data-points'"
      title="{{ 'No unimported devices found.' | translate }}"
      [horizontal]="true"
    >
    </c8y-ui-empty-state> --> `,
})
export class AdamosToCumulocityComponent implements OnInit {
  @Output() selectedDevice = new EventEmitter<string>();
  protected isLoading = false;

  devices: IResultList<IManagedObject>;
  selected = { id: "", name: "" };

  constructor(private inventory: InventoryService) {}

  ngOnInit(): void {
    this.isLoading = true;
    this.loadDevices()
      .then((devices) => (this.devices = devices))
      .finally(() => (this.isLoading = false));
  }

  private loadDevices() {
    const queryFilter: object = {
      fragmentType: "c8y_IsDevice",
      withChildren: false,
      pageSize: 200,
      q: "$filter=not has(adamos_hub_isDevice) $orderby=name",
    };
    return this.inventory.list(queryFilter);
  }

  isChecked(device: IManagedObject) {
    return this.selected?.id === device.id;
  }

  onSelect(checked: boolean, device: IManagedObject) {
    if (checked) {
      this.selected = { id: device.id, name: device.name };
      this.selectedDevice.emit(device.id);
    } else if (this.selected?.id === device.id) {
      this.selected = null;
      this.selectedDevice.emit(null);
    }
  }
}
