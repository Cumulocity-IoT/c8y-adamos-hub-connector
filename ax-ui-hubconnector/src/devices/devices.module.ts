import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { DevicesListComponent } from "./devices-list.component";
import { DevicesDetailComponent } from "./devices-detail.component";
import { CoreModule } from "@c8y/ngx-components";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { routes } from "./routes";
import { SharedModule } from "../shared/shared.module";
import { AdamosHubService } from "../shared/adamosHub.service";

/* 
 * Defines a module for the device-management.
 */
@NgModule({
    imports : [ 
        BrowserModule,
        CoreModule,
        RouterModule,
        FormsModule,
        RouterModule.forChild(routes),
        SharedModule
    ],
    declarations : [
        DevicesListComponent,
        DevicesDetailComponent
    ],
    exports : [
        DevicesListComponent
    ],
    providers : [
        AdamosHubService
    ]
})
export class DevicesModule {}