import { NgModule } from "@angular/core";
import { DevicesListComponent } from "./devices-list.component";
import { DevicesDetailComponent } from "./devices-detail.component";
import { CoreModule, HOOK_ROUTE } from "@c8y/ngx-components";
import { AssetSelectorModule } from "@c8y/ngx-components/assets-navigator";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { routes } from "./routes";
import { SharedModule } from "../shared/shared.module";
import { AdamosHubService } from "../shared/adamosHub.service";
import { NewDevicesListComponent } from "./new-devices-list.component";
import { DevicesListDatasourceService } from "./devices-list-datasource.service";
import { LinkedAdamosDeviceFilter } from "./filters/linked-adamos-device-filter.component";
import { LinkedC8yDeviceFilter } from "./filters/linked-c8y-device-filter.component";
import { LinkDeviceModalComponent } from "./modal/link-device-modal.component";
import { AdamosToCumulocityComponent } from "./modal/adamos-to-c8y.component";
import { CumulocityToAdamosComponent } from "./modal/c8y-to-adamos.component";

/*
 * Defines a module for the device-management.
 */
@NgModule({
  imports: [
    CoreModule,
    RouterModule,
    AssetSelectorModule,
    FormsModule,
    SharedModule,
  ],
  declarations: [
    DevicesListComponent,
    DevicesDetailComponent,
    NewDevicesListComponent,
    LinkedAdamosDeviceFilter,
    LinkedC8yDeviceFilter,
    LinkDeviceModalComponent,
    AdamosToCumulocityComponent,
    CumulocityToAdamosComponent
  ],
  exports: [DevicesListComponent],
  providers: [
    DevicesListDatasourceService,
    AdamosHubService,
    {
      provide: HOOK_ROUTE,
      useValue: routes,
      multi: true,
    },
  ],
})
export class DevicesModule {}
