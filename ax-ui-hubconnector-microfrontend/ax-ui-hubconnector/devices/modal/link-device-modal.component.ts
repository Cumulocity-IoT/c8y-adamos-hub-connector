import { Component } from "@angular/core";
import { IManagedObject } from "@c8y/client";
import { AlertService } from "@c8y/ngx-components";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { NewAdamosHubService } from "../../../ax-ui-hubconnector/shared/new-adamos-hub.service";
import { AdamosHubDevice } from "../../../ax-ui-hubconnector/shared/model/AdamosDevice";

@Component({
  templateUrl: "./link-device-modal.component.html",
})
export class LinkDeviceModalComponent {
  closeSubject: Subject<boolean> = new Subject();
  device: IManagedObject | AdamosHubDevice;
  protected title = "";
  protected infoMessage = "";

  protected mode: "C8yToAdamos" | "AdamosToC8y";

  protected selectedDevice: string | number;

  constructor(
    private alert: AlertService,
    public modal: BsModalRef,
    private hubService: NewAdamosHubService
  ) {}

  setMode(mode: "C8yToAdamos" | "AdamosToC8y"): void {
    this.mode = mode;
    if (mode === "C8yToAdamos") {
      this.title = `${(<IManagedObject>this.device).name} → ADAMOS Hub`;
      this.infoMessage = `You can either create a new device on ADAMOS Hub,
    or select an existing device which will merge both devices.`;
    } else {
      this.title = `${
        (<AdamosHubDevice>this.device).customerIdentification.name
      } → Cumulocity IoT`;
      this.infoMessage = `You can either create a new device on Cumulocity IoT,
    or select an existing device which will merge both devices.`;
    }
  }

  onCancel() {
    this.closeSubject.next(false);
    this.modal.hide();
  }

  onClose() {
    // called if save is pressed
    this.closeSubject.next();
  }

  onSelectDevice(device: string | number): void {
    this.selectedDevice = device;
  }

  createNewDevice(): void {
    if (this.mode === "AdamosToC8y") {
      this.hubService
        .linkHubDevice(this.device.uuid)
        .then((c8yDevice) => {
          this.alert.success(
            "Linking successful",
            `Linked ADAMOS Hub device with uuid=${this.device.uuid} → new Cumulocity IoT Device with id=${c8yDevice.id["value"]}`
          );
          this.closeSubject.next(true);
          this.modal.hide();
        })
        .catch((error) => {
          this.alert.danger(error);
          this.closeSubject.next(false);
        })
        .finally(() => this.modal.hide());
    } else if (this.mode === "C8yToAdamos") {
      this.hubService
        .linkCumulocityDevice((<IManagedObject>this.device).id)
        .then((c8yDevice) => {
          this.alert.success(
            "Linking successful",
            `Linked Cumulocity IoT Device with id=${c8yDevice.id} → new ADAMOS Hub device}`
          );
          this.closeSubject.next(true);
        })
        .catch((error) => {
          this.alert.danger(error);
          this.closeSubject.next(false);
        })
        .finally(() => this.modal.hide());
    }
  }

  linkToExistingDevice(): void {}
}
