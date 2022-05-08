import { NgModule } from '@angular/core';
//import { CommonModule } from '@angular/common';
import {
  CommonModule,
  CoreModule,
} from '@c8y/ngx-components';

import { NgxJsonViewerComponent } from './ngx-json-viewer.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    CoreModule,
    SharedModule
  ],
  declarations: [
    NgxJsonViewerComponent
  ],
  exports: [
    NgxJsonViewerComponent
  ]
})
export class NgxJsonViewerModule { }
