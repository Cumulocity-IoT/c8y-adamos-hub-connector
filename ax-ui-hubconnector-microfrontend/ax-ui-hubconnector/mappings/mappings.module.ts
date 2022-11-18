import { NgModule } from "@angular/core";
import { MappingsListComponent } from "./mappings-list.component";
import { MappingsDetailComponent } from "./mappings-detail.component";
import { CoreModule, HOOK_ROUTE } from "@c8y/ngx-components";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { routes } from "./routes";
import { NgxJsonViewerModule } from "../ngx-json-viewer/ngx-json-viewer.module";
import { SharedModule } from "../shared/shared.module";
import { AdamosHubService } from "../shared/adamosHub.service";

/*
 * Defines a module for the device-management.
 */
@NgModule({
  imports: [
    CoreModule,
    RouterModule,
    FormsModule,
    NgxJsonViewerModule,
    SharedModule,
  ],
  declarations: [MappingsListComponent, MappingsDetailComponent],
  exports: [MappingsListComponent],
  providers: [
    AdamosHubService,
    {
      provide: HOOK_ROUTE,
      useValue: routes,
      multi: true,
    },
  ],
})
export class MappingsModule {}
