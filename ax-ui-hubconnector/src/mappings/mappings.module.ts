import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { MappingsListComponent } from "./mappings-list.component";
import { MappingsDetailComponent } from "./mappings-detail.component";
import { CoreModule } from "@c8y/ngx-components";
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
    imports : [ 
        BrowserModule,
        CoreModule,
        RouterModule,
        FormsModule,
        RouterModule.forChild(routes), 
        NgxJsonViewerModule,
        SharedModule
    ],
    declarations : [
        MappingsListComponent,
        MappingsDetailComponent
    ],
    exports : [
        MappingsListComponent
    ],
    providers : [
        AdamosHubService
    ]
})
export class MappingsModule {}