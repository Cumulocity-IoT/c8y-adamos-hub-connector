import { Component, OnInit } from "@angular/core";
import { Column, FilteringFormRendererContext } from "@c8y/ngx-components";
import { has } from "lodash-es";

@Component({
  templateUrl: "./linked-c8y-device-filter.component.html",
})
export class LinkedC8yDeviceFilter implements OnInit {
  onlyConnected: boolean = null;
  onlyDisconnected: boolean = null;

  constructor(public context: FilteringFormRendererContext) {}

  ngOnInit() {
    const column = this.context.property;
    const hasFilterSet = !!column.externalFilterQuery;
    if (hasFilterSet) {
      this.writeColumnFilterValuesToView(column);
    }
  }

  writeColumnFilterValuesToView(column: Column): void {
    if (!column.externalFilterQuery) {
      return;
    }
    if (has(column.externalFilterQuery, "__not")) {
      this.onlyDisconnected = true;
    } else if (has(column.externalFilterQuery, "__has")) {
      this.onlyConnected = true;
    }
  }

  onOnlyConnectedChange(checked: boolean): void {
    this.onlyConnected = checked;
    if (checked) {
      this.onlyDisconnected = false;
    }
  }

  onOnlyDisconnectedChange(checked: boolean): void {
    this.onlyDisconnected = checked;
    if (checked) {
      this.onlyConnected = false;
    }
  }

  resetFilter(): void {
    this.context.resetFilter();
  }

  applyFilter(): void {
    let filter = null;
    if (this.onlyConnected) {
      filter = { __has: "adamos_hub_connectorSettings" };
    } else if (this.onlyDisconnected) {
      filter = { __not: { __has: "adamos_hub_connectorSettings" } };
    }
    this.context.applyFilter({
      externalFilterQuery: filter,
    });
  }
}
