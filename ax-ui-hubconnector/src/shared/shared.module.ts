import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { CoreModule, CommonModule } from "@c8y/ngx-components";
import { AdamosHubService } from "./adamosHub.service";
import { ConvertCamelCaseToSpace } from "./camel-case-to-space.pipe";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { BsDatepickerModule } from "ngx-bootstrap/datepicker";
import { TooltipModule } from "ngx-bootstrap/tooltip";

/* 
 * Defines a module for the device-management.
 */
@NgModule({
    imports : [ 
        BrowserModule,
        CoreModule,
        CommonModule,
        BsDropdownModule.forRoot(),
        BsDatepickerModule.forRoot()
    ],
    declarations: [
        ConvertCamelCaseToSpace
    ],
    exports: [
        ConvertCamelCaseToSpace,
        BsDropdownModule,
        BsDatepickerModule,
        TooltipModule
    ],
    providers : [
        AdamosHubService
    ]
})
export class SharedModule {}