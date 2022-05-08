import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { CoreModule } from "@c8y/ngx-components";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { routes } from "./routes";
import { SharedModule } from "../shared/shared.module";
import { AdamosHubService } from "../shared/adamosHub.service";
import { EventRulesListComponent } from "./eventRules-list.component";

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
        EventRulesListComponent
    ],
    exports : [
        EventRulesListComponent
    ],
    providers : [
        AdamosHubService
    ]
})
export class EventRulesModule {}