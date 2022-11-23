import { Component, OnInit } from "@angular/core";
import { Column, FilteringFormRendererContext } from "@c8y/ngx-components";
import { AdamosHubDevice } from "../../../ax-ui-hubconnector/shared/model/AdamosDevice";

@Component({
  templateUrl: "./linked-adamos-device-filter.component.html",
})
export class LinkedAdamosDeviceFilter implements OnInit {
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
    const { onlyConnected, onlyDisconnected } = column.externalFilterQuery;
    this.onlyConnected = onlyConnected;
    this.onlyDisconnected = onlyDisconnected;
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
    this.context.applyFilter({
      externalFilterQuery: {
        onlyConnected: this.onlyConnected,
        onlyDisconnected: this.onlyDisconnected,
      },
      filterPredicate: (item: AdamosHubDevice) => {
        if (this.onlyConnected) {
          return item.linked === true;
        } else {
          return item.linked === false;
        }
      },
    });
  }
}
