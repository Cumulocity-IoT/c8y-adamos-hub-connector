import { NgModule } from "@angular/core";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { RouterModule as ngRouterModule } from "@angular/router";
import { BsModalRef } from "ngx-bootstrap/modal";
import {
  BootstrapComponent,
  CoreModule,
  RouterModule,
} from "@c8y/ngx-components";
import { HubConnectorPluginModule } from "./ax-ui-hubconnector/hubconnector-plugin.module";

@NgModule({
  imports: [
    BrowserAnimationsModule,
    ngRouterModule.forRoot([], { enableTracing: false, useHash: true }),
    RouterModule.forRoot(),
    CoreModule.forRoot(),
    HubConnectorPluginModule
  ],
  providers: [BsModalRef],
  bootstrap: [BootstrapComponent],
})
export class AppModule {}
