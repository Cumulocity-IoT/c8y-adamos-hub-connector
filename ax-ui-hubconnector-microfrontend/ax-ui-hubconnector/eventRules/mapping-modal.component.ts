import { Component, EventEmitter, Input, Output } from "@angular/core";
import { IManagedObject, InventoryService, IResultList } from "@c8y/client";
import { isEmpty } from "lodash-es";
import { Observable, pipe, UnaryFunction } from "rxjs";
import { map } from "rxjs/operators";
import { EventMapping } from "./event-rules-to-hub.component";

type UIMapping = {
  c8yEventType: string;
  c8yFragments: { value: string }[];
  c8yDevices: string[];
  enabled: boolean;
  name: string;
  adamosEventType: string;
  id: string;
};

@Component({
  templateUrl: "./mapping-modal.component.html",
  selector: "mapping-modal",
})
export class MappingModalComponent {
  ui: UIMapping;
  @Input() set selectedMapping(value: EventMapping) {
    this.ui = {
      c8yEventType: value.c8yEventType,
      c8yFragments: value.c8yFragments.map((f) => ({ value: f })),
      c8yDevices: value.c8yDevices,
      adamosEventType: value.adamosEventType,
      enabled: value.enabled,
      name: value.name,
      id: value.id,
    };
  }

  @Output() save = new EventEmitter<EventMapping>();
  @Output() cancel = new EventEmitter<void>();

  devices: IResultList<IManagedObject>;

  filterPipe: UnaryFunction<Observable<[]>, Observable<never[]>>;
  pattern = "";

  constructor(private inventory: InventoryService) {
    this.inventory
      .list({
        query: "$filter=has('adamos_hub_data')",
        pageSize: 100,
        withTotalPages: true,
      })
      .then((result) => (this.devices = result));
  }

  setPipe(filterStr: string) {
    this.pattern = filterStr;
    this.filterPipe = pipe(
      map((data: []) =>
        data.filter(
          (mo: IManagedObject) =>
            mo.name?.toLowerCase().indexOf(filterStr.toLowerCase()) > -1 ||
            mo.id.indexOf(filterStr) > -1
        )
      )
    );
  }

  onChangeCheckbox(device: IManagedObject, checked: boolean): void {
    const devices = this.ui.c8yDevices;
    if (checked) {
      devices.push(device.id);
    } else {
      this.ui.c8yDevices = devices.filter((d) => d !== device.id);
    }
  }

  onEditOKClick(): void {
    const {
      c8yEventType,
      c8yFragments,
      c8yDevices,
      enabled,
      name,
      id,
      adamosEventType,
    } = this.ui;

    // if field is left empty it results is { value: "" } which we need to filter out
    const fragments = c8yFragments.filter((v) => !isEmpty(v.value)).map((v) => v.value);

    this.save.emit({
      c8yEventType,
      c8yFragments: fragments,
      c8yDevices,
      enabled,
      name,
      id,
      adamosEventType,
    });
  }

  onEditCancelClick(): void {
    this.cancel.emit();
  }

  isChecked(device: IManagedObject): boolean {
    return this.ui.c8yDevices.includes(device.id);
  }

  removeFragment(fragment: { value: string }) {
    const fragments = this.ui.c8yFragments;
    this.ui.c8yFragments = fragments.filter((f) => f !== fragment);
  }

  addFragment(): void {
    this.ui.c8yFragments.push({ value: "" });
  }

  mandatoryFieldsFilled(): boolean {
    const m = this.ui;
    return (
      !isEmpty(m.name) &&
      !isEmpty(m.c8yEventType) &&
      !isEmpty(m.adamosEventType)
    );
  }
}
