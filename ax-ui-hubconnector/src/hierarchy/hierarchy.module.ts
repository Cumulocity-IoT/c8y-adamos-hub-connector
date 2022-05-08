import { NgModule } from '@angular/core';
import { HierarchyTreeComponent } from './hierarchy-tree.component';
import { BrowserModule } from '@angular/platform-browser';
import { CoreModule } from '@c8y/ngx-components';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { FormsModule } from '@angular/forms';
import { routes } from './routes';
import { AdamosHubService } from '../shared/adamosHub.service';
import { HierarchyItemComponent } from './hierarchy-item.component';
import { DragDropModule } from '@angular/cdk/drag-drop';

@NgModule({
  imports: [
    BrowserModule,
    CoreModule,
    RouterModule,
    FormsModule,
    RouterModule.forChild(routes),
    SharedModule,
    DragDropModule
  ],
  declarations: [
    HierarchyTreeComponent,
    HierarchyItemComponent
  ],
  exports : [
    HierarchyTreeComponent
  ],
  providers : [
    AdamosHubService
  ]
})
export class HierarchyModule { }
