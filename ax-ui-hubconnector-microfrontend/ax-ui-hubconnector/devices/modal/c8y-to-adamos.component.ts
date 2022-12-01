import { Component, EventEmitter, OnInit, Output } from "@angular/core";
import { AlertService } from "@c8y/ngx-components";
import { AdamosHubDevice } from "../../shared/model/AdamosDevice";
import { NewAdamosHubService } from "../../shared/new-adamos-hub.service";

@Component({
  selector: "c8y-to-adamos-form",
  template: `
    <div class="form-group" *ngIf="isLoading">
      <i c8y-icon="spinner" class="fa fw fa-spinner fa-spin"></i>
      {{ "Importing ADAMOS Hub devices..." | translate }}
    </div>

    <div class="form-group" *ngIf="adamosDevices.length">
      <label>{{ "Unlinked ADAMOS Hub Devices" | translate }}</label>
      <div class="c8y-select-wrapper">
        <select
          class="form-control"
          (change)="onSelect($event.target.value)"
          [(ngModel)]="selectedHubDeviceUuid"
        >
          <option
            *ngFor="let equipment of adamosDevices"
            value="{{ equipment.uuid }}"
          >
            {{ equipment.customerIdentification.name }}
          </option></select
        ><span></span>
      </div>
    </div>

    <c8y-ui-empty-state
      *ngIf="!isLoading && adamosDevices.length === 0"
      [icon]="'c8y-data-points'"
      title="{{ 'No unimported devices found.' | translate }}"
      [horizontal]="true"
    >
    </c8y-ui-empty-state>
  `,
})
export class CumulocityToAdamosComponent implements OnInit {
  protected isLoading = false;
  protected adamosDevices: AdamosHubDevice[] = [];
  protected selectedHubDeviceUuid: number;
  @Output() selectedDevice = new EventEmitter<string>();

  constructor(
    private hubService: NewAdamosHubService,
    private alert: AlertService
  ) {}
  ngOnInit(): void {
    this.isLoading = true;
    this.hubService
      .getDisconnectedEquipment()
      .then((devices) => (this.adamosDevices = devices))
      .catch((error) => this.alert.danger(error))
      .finally(() => (this.isLoading = false));
  }

  onSelect(uuid: string): void {
    this.selectedDevice.emit(uuid);
  }
}
