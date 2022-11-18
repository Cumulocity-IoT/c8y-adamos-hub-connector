import { NgModule } from '@angular/core';
import { HierarchyTreeComponent } from './hierarchy-tree.component';
import { CoreModule, HOOK_ROUTE } from '@c8y/ngx-components';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { FormsModule } from '@angular/forms';
import { routes } from './routes';
import { AdamosHubService } from '../shared/adamosHub.service';
import { HierarchyItemComponent } from './hierarchy-item.component';
import { DragDropModule } from '@angular/cdk/drag-drop';

@NgModule({
  imports: [
    CoreModule,
    RouterModule,
    FormsModule,
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
    AdamosHubService,
    {
      provide: HOOK_ROUTE,
      useValue: routes,
      multi: true,
    },
  ]
})
export class HierarchyModule { }
