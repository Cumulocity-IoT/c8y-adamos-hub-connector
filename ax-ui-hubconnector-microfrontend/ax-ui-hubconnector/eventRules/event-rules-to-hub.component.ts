import { Component, ViewChild } from "@angular/core";
import { NewAdamosHubService } from "../shared/new-adamos-hub.service";
import { capitalize, cloneDeep } from "lodash-es";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { v4 as uuid } from "uuid";
import { ModalService, Status, _ } from "@c8y/ngx-components";

export interface IEventMapping {
  /** The Cumulocity event type that should be the source of the mapping */
  c8yEventType: string;

  /* Optionally select one or multiple devices for which the event should be mapped. If no device is selected, rule should apply to all devices */
  c8yDevices: string[];

  /* The ADAMOS Hub event code (=type) */
  adamosEventType: string;

  /* list of fragments from the C8Y event that should be mapped 1:1 into the "attributes" array of the ADMOS Hub event */
  c8yFragments: string[];

  name: string;

  id: string;

  enabled: boolean;
}

export class EventMapping implements IEventMapping {
  c8yEventType: string;
  c8yDevices: string[] = [];
  adamosEventType: string;
  c8yFragments: string[] = [""];
  enabled = false;
  name = "";
  id = uuid();
}

@Component({
  templateUrl: "./event-rules-to-hub.component.html",
})
export class EventRulesToHubComponent {
  response: IEventMapping[];
  rules: IEventMapping[];

  selectedMapping: IEventMapping = null;

  @ViewChild("mappingModal") mappingModal: any;
  modalRef: BsModalRef;

  isLoading = false;
  hasChanges = false;

  constructor(
    private bsModalService: BsModalService,
    private adamosService: NewAdamosHubService,
    private modalService: ModalService
  ) {
    this.refresh();
  }

  async fetchRules() {
    const response = await this.adamosService.getMappingRules();
    this.response = response;
    this.rules = cloneDeep(response);
  }

  onClickMoveUp(rule: IEventMapping) {
    this.hasChanges = true;
    this.array_move(
      this.rules,
      this.rules.indexOf(rule),
      this.rules.indexOf(rule) - 1
    );
  }

  openTogglStatusModal(rule: IEventMapping) {
    const action = rule.enabled ? "deactivate" : "activate";
    const title = rule.name;
    const body = _(`Are you sure you want to ${action} this rule?`);
    const labels = {
      ok: _(capitalize(action)),
    };

    rule.enabled = !rule.enabled;
    this.modalService.confirm(title, body, Status.INFO, labels).then(
      () => (this.hasChanges = true),
      () => (rule.enabled = !rule.enabled)
    );
  }

  onClickMoveDown(rule: IEventMapping) {
    this.hasChanges = true;
    this.array_move(
      this.rules,
      this.rules.indexOf(rule),
      this.rules.indexOf(rule) + 1
    );
  }

  onClickDelete(rule: IEventMapping) {
    this.rules.splice(this.rules.indexOf(rule), 1);
    this.hasChanges = true;
  }

  onClickDuplicate(rule: IEventMapping) {
    const duplicate = cloneDeep(rule);
    duplicate.id = uuid();
    duplicate.name = `${duplicate.name} (Copy)`;

    this.rules.splice(this.rules.indexOf(rule) + 1, 0, duplicate);
    this.hasChanges = true;
  }

  private array_move(arr: unknown[], old_index: number, new_index: number) {
    if (new_index >= arr.length) {
      let k = new_index - arr.length + 1;
      while (k--) {
        arr.push(undefined);
      }
    }
    arr.splice(new_index, 0, arr.splice(old_index, 1)[0]);
    return arr;
  }

  async refresh() {
    this.isLoading = true;
    this.fetchRules()
      .then(() => (this.hasChanges = false))
      .finally(() => (this.isLoading = false));
  }

  onClickAdd(): void {
    this.selectedMapping = new EventMapping();
    this.modalRef = this.bsModalService.show(this.mappingModal);
  }

  onClickEdit(rule: IEventMapping): void {
    this.selectedMapping = rule;
    this.modalRef = this.bsModalService.show(this.mappingModal);
  }

  onClickUndo(): void {
    this.rules = cloneDeep(this.response);
    this.hasChanges = false;
  }

  onModalCancel() {
    this.modalRef.hide();
  }

  onModalSave(mapping: IEventMapping) {
    const oldRule = this.rules.find((r) => r.id === mapping.id);
    if (oldRule) {
      // if user saved in edit mode
      this.rules[this.rules.indexOf(oldRule)] = mapping;
    } else {
      // if user saved in create mode
      this.rules.push(mapping);
    }

    this.modalRef.hide();
    this.hasChanges = true;
  }

  async onSave() {
    this.response = cloneDeep(this.rules);
    await this.adamosService.updateMappingRules(this.response);
    await this.fetchRules();
    this.hasChanges = false;
  }
}
