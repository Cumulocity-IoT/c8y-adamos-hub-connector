import { NgModule } from "@angular/core";
import { CoreModule } from "@c8y/ngx-components";

import { NgxJsonViewerComponent } from "./ngx-json-viewer.component";
import { SharedModule } from "../shared/shared.module";

@NgModule({
  imports: [CoreModule, SharedModule],
  declarations: [NgxJsonViewerComponent],
  exports: [NgxJsonViewerComponent],
})
export class NgxJsonViewerModule {}
