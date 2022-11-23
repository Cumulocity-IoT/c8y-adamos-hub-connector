import { _, ModalService } from "@c8y/ngx-components";
import { Component, ViewChild } from "@angular/core";
import { InventoryService, IManagedObject } from "@c8y/client";
import { Observable, BehaviorSubject, forkJoin } from "rxjs";
import { AdamosHubService } from "../shared/adamosHub.service";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Router } from "@angular/router";

/*
 * Lists all devices of the current tenant with the posibility to add or remove Hub-Settings.
 */
@Component({
  selector: "devices",
  templateUrl: "./devices-list.component.html",
})
export class DevicesListComponent {
  devices: IManagedObject[];
  informationText: string;
  logs: Observable<String[]>;
  activeFilter: string = "ALL";
  title: string = "All Devices";
  equipmentList: Array<any>;
  isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);

  isDialogDataLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  isDialogDataImporting$: BehaviorSubject<boolean> = new BehaviorSubject(false);

  @ViewChild("dlgImport") dlgImport: any;
  modalRef: BsModalRef;
  selectedHubDeviceUuid: any;

  // The filter object will add query parameters
  // to the request which is made by the service.
  private filter: object = {
    query: "$filter=has('c8y_IsDevice')",
    // paging information will be a part of the response now
    withTotalPages: true,
    pageSize: 100,
  };

  constructor(
    private inventory: InventoryService,
    private hubService: AdamosHubService,
    private bsModalService: BsModalService,
    private modalService: ModalService,
    private router: Router
  ) {
    // _ annotation to mark this string as translatable string.
    this.informationText = _(
      "Ooops! It seems that there is no device to display."
    );
    if (this.hubService.getLastDetailFilter() != undefined) {
      this.activeFilter = this.hubService.getLastDetailFilter();
    }

    this.loadDevices();
  }

  // Promise-based usage of InventoryService.
  async loadDevices() {
    this.isLoading$.next(true);

    let orderBy: String = " $orderby=id asc";
    switch (this.activeFilter) {
      case "DISCONNECTED":
        this.title = "Disconnected Devices";
        this.filter = {
          ...this.filter,
          query:
            "$filter=has('c8y_IsDevice') and not has('adamos_hub_isDevice')" +
            orderBy,
        };
        break;
      case "CONNECTED":
        this.title = "Connected Devices";
        this.filter = {
          ...this.filter,
          query: "$filter=has('adamos_hub_isDevice')" + orderBy,
        };
        break;
      default:
        this.title = "All Devices";
        this.filter = {
          ...this.filter,
          query:
            "$filter=has('c8y_IsDevice') or has('adamos_hub_isDevice')" +
            orderBy,
        };
        break;
    }

    const { data } = await this.inventory.list(this.filter);
    this.devices = data;

    this.isLoading$.next(false);
  }

  // Add Hub-Settings manually
  async addHub(id: number) {
    this.hubService.addAsset(id).subscribe((item) => this.loadDevices());
  }

  // Remove Hub-Settings manually
  async removeHub(id: number) {
    this.hubService.removeAsset(id).subscribe((item) => this.loadDevices());
  }

  filterActive(button: string) {
    return button === this.activeFilter;
  }

  async changeFilter(button: string) {
    this.activeFilter = button;
    this.hubService.setLastDetailFilter(button);
    await this.loadDevices();
  }

  onRefresh() {
    this.loadDevices();
  }

  async onImportClick() {
    this.selectedHubDeviceUuid = null;
    this.isDialogDataLoading$.next(true);
    forkJoin([
      this.hubService.getDisconnectedEquipment$(),
      this.hubService.getGlobalSettings$(),
    ]).subscribe((results) => {
      this.equipmentList = results[0];
      this.isDialogDataLoading$.next(false);
    });

    this.modalRef = this.bsModalService.show(this.dlgImport);
  }

  onImportDeviceClick() {
    if (this.selectedHubDeviceUuid != null) {
      this.isDialogDataImporting$.next(true);
      this.hubService
        .importHubDevice$(this.selectedHubDeviceUuid)
        .subscribe((device) => {
          this.router.navigate(["/devices", device.id.value]);
          this.isDialogDataImporting$.next(false);
          this.modalRef.hide();
        });
    } else {
      this.modalService.confirm(
        _("Info"),
        _("Please select a device to import?"),
        "info",
        { ok: _("OK") }
      );
    }
  }
}
