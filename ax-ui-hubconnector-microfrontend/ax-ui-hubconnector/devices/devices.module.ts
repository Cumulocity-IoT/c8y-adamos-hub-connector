import { NgModule } from "@angular/core";
import { DevicesListComponent } from "./devices-list.component";
import { DevicesDetailComponent } from "./devices-detail.component";
import { CoreModule, HOOK_ROUTE } from "@c8y/ngx-components";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { routes } from "./routes";
import { SharedModule } from "../shared/shared.module";
import { AdamosHubService } from "../shared/adamosHub.service";

/*
 * Defines a module for the device-management.
 */
@NgModule({
  imports: [CoreModule, RouterModule, FormsModule, SharedModule],
  declarations: [DevicesListComponent, DevicesDetailComponent],
  exports: [DevicesListComponent],
  providers: [
    AdamosHubService,
    {
      provide: HOOK_ROUTE,
      useValue: routes,
      multi: true,
    },
  ],
})
export class DevicesModule {}
