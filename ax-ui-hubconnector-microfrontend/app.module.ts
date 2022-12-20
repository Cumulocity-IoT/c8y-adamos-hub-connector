import { NgModule } from "@angular/core";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { RouterModule as ngRouterModule } from "@angular/router";
import { BsModalRef } from "ngx-bootstrap/modal";
import {
  BootstrapComponent,
  CoreModule,
  RouterModule,
} from "@c8y/ngx-components";

@NgModule({
  imports: [
    BrowserAnimationsModule,
    ngRouterModule.forRoot([], { enableTracing: false, useHash: true }),
    RouterModule.forRoot(),
    CoreModule.forRoot()
  ],
  providers: [BsModalRef],
  bootstrap: [BootstrapComponent],
})
export class AppModule {}
