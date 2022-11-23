import { Component, EventEmitter } from "@angular/core";
import {
  ActionControl,
  AlertService,
  Column,
  gettext,
  ModalService,
  Pagination,
  Row,
  Status,
} from "@c8y/ngx-components";
import { has } from "lodash-es";
import {
  IdentityService,
  IExternalIdentity,
  IManagedObject,
  InventoryService,
} from "@c8y/client";
import { TranslateService } from "@ngx-translate/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { DevicesListDatasourceService } from "./devices-list-datasource.service";
import { AdamosHubDevice } from "../shared/model/AdamosDevice";
import { LinkedC8yDeviceFilter } from "./filters/linked-c8y-device-filter.component";
import { LinkedAdamosDeviceFilter } from "./filters/linked-adamos-device-filter.component";
import { NewAdamosHubService } from "../shared/new-adamos-hub.service";
import { LinkDeviceModalComponent } from "./modal/link-device-modal.component";
import { take } from "rxjs/operators";

@Component({
  providers: [DevicesListDatasourceService],
  templateUrl: "./new-devices-list.component.html",
})
export class NewDevicesListComponent {
  c8yColumns: Column[];
  adamosColumns: Column[];

  pagination: Pagination = {
    pageSize: 50,
    currentPage: 1,
  };

  actionControls: ActionControl[] = [
    {
      type: "LINK",
      text: `Link device`,
      icon: "connected",
      callback: (item: IManagedObject) => this.openLinkDeviceModal(item),
      showIf: (item: Row) =>
        (this.activeFilter === "CUMULOCITY_DEVICES" &&
          !item.adamos_hub_connectorSettings) ||
        (this.activeFilter === "ADAMOS_DEVICES" && !item.linked),
    },
    {
      type: "UNLINK",
      text: "Disconnect device",
      icon: "minus-circle",
      callback: (item: Row) => this.unlinkDevice(item),
      showIf: (item: Row) =>
        (this.activeFilter === "CUMULOCITY_DEVICES" &&
          item.adamos_hub_connectorSettings) ||
        (this.activeFilter === "ADAMOS_DEVICES" && item.linked),
    },
  ];

  refresh = new EventEmitter<void>();

  activeFilter: "CUMULOCITY_DEVICES" | "ADAMOS_DEVICES" = "CUMULOCITY_DEVICES";

  adamosDevices: AdamosHubDevice[] = [];

  constructor(
    public devicesDataSource: DevicesListDatasourceService,
    protected modal: ModalService,
    protected translateService: TranslateService,
    protected alertService: AlertService,
    private bsModalService: BsModalService,
    private adamosHubService: NewAdamosHubService,
    private identity: IdentityService
  ) {
    this.c8yColumns = this.getDefaultC8yColumns();
    this.adamosColumns = this.getAdamosColumns();
  }

  private fetchAdamosDevices(
    adamosHubService: NewAdamosHubService
  ): Promise<AdamosHubDevice[]> {
    return Promise.all([
      adamosHubService.getEquipment(),
      adamosHubService.getDisconnectedEquipment(),
    ]).then((results) => {
      const [all, disconnected] = results;
      const disconnectedUUIDs = disconnected.map((d) => d.uuid);
      return all.map((device) => {
        device.linked = !disconnectedUUIDs.includes(device.uuid);
        return device as AdamosHubDevice;
      });
    });
  }

  getDefaultC8yColumns(): Column[] {
    return [
      {
        name: "id",
        header: "ID",
        path: "id",
        filterable: true,
        gridTrackSize: "0.4fr",
      },
      {
        name: "name",
        header: "Name",
        path: "name",
        filterable: true,
      },
      {
        name: "linked",
        header: "Linked",
        sortable: false,
        filterable: true,
        filteringFormRendererComponent: LinkedC8yDeviceFilter,
      },
    ];
  }

  getAdamosColumns(): Column[] {
    return [
      {
        name: "uuid",
        header: "UUID",
        path: "uuid",
        filterable: true,
        gridTrackSize: "1fr",
      },
      {
        name: "name",
        header: "Name",
        filterable: true,
        path: "customerIdentification.name",
      },
      {
        name: "linked",
        header: "Linked",
        path: "linked",
        filterable: true,
        filteringFormRendererComponent: LinkedAdamosDeviceFilter,
      },
    ];
  }

  changeFilter(filter: "CUMULOCITY_DEVICES" | "ADAMOS_DEVICES"): void {
    this.activeFilter = filter;
    if (filter === "ADAMOS_DEVICES") {
      this.reloadAdamosDevices();
    }
  }

  reloadAdamosDevices() {
    this.fetchAdamosDevices(this.adamosHubService)
      .then((devices) => (this.adamosDevices = devices))
      .catch((error) => this.alertService.danger(error));
  }

  async unlinkDevice(item: Row): Promise<void> {
    const deviceName =
      this.activeFilter === "CUMULOCITY_DEVICES"
        ? (item as IManagedObject).name
        : (item as any as AdamosHubDevice).customerIdentification.name;

    try {
      const source =
        this.activeFilter === "CUMULOCITY_DEVICES"
          ? "ADAMOS Hub"
          : "Cumulocity IoT";
      await this.modal.confirm(
        gettext("Disconnect device"),
        this.translateService.instant(
          gettext(
            `You are about to disconnect "${deviceName}" from ${source}. Do you want to proceed?`
          )
        ),
        Status.DANGER,
        { ok: gettext("Disconnect"), cancel: gettext("Cancel") }
      );

      if (this.activeFilter === "CUMULOCITY_DEVICES") {
        await this.adamosHubService.unlink((item as IManagedObject).id);
      } else {
        const hubDevice = item as any as AdamosHubDevice;
        const external: IExternalIdentity = {
          externalId: hubDevice.uuid,
          type: "adamos_hub_machineTool_uuid",
        };
        const { data } = await this.identity.detail(external);
        await this.adamosHubService.unlink(data.managedObject.id.toString());
      }
      this.alertService.success(gettext("Device disconnect successful."));
      // force - reload the grid
      this.reloadTable();
    } catch (ex) {
      // only if not cancel from modal
      if (ex) {
        this.alertService.addServerFailure(ex);
      }
    }
  }

  reloadTable(): void {
    if (this.activeFilter === "CUMULOCITY_DEVICES") {
      this.refresh.next();
    } else {
      this.reloadAdamosDevices();
    }
  }

  openLinkDeviceModal(device: IManagedObject | AdamosHubDevice) {
    const modalRef = this.bsModalService.show(LinkDeviceModalComponent, {});
    modalRef.content.device = device;
    modalRef.content.setMode(
      has(device, "uuid") ? "AdamosToC8y" : "C8yToAdamos"
    );
    modalRef.content.closeSubject.pipe(take(1)).subscribe((success) => {
      if (success) this.reloadTable();
    });
  }
}
