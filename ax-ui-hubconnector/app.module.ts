import { NgModule, LOCALE_ID } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule as ngRouterModule } from '@angular/router';
import { Routes } from '@angular/router';
import { CoreModule, BootstrapComponent, RouterModule, PluginsModule } from '@c8y/ngx-components';
import { HubConnectorBreadcrumbFactory } from './factories/Breadcrumb';
import { HubConnectorNavigationFactory } from './factories/Navigation';
import {
  CommonModule,
  HOOK_BREADCRUMB,
  HOOK_NAVIGATOR_NODES,
} from '@c8y/ngx-components';

import { SettingsModule } from './src/settings/settings.module';
import { FormsModule } from '@angular/forms';
import { DevicesModule } from './src/devices/devices.module';
import { MappingsModule } from './src/mappings/mappings.module';
import { EventRulesModule } from './src/eventRules/eventRules.module';
import { HierarchyModule } from './src/hierarchy/hierarchy.module';

/**
 * Angular Routes.
 * Within this array at least path (url) and components are linked.
 */
const appRoutes: Routes = [
    {
        path: '',
        redirectTo: 'devices',
        pathMatch: 'full'
    }
];
@NgModule({
  imports: [
    BrowserAnimationsModule,
    RouterModule.forRoot(),
    ngRouterModule.forRoot([], { enableTracing: false, useHash: true }),
    CoreModule.forRoot(),
    PluginsModule.forRoot(),
    
    CommonModule,
    FormsModule,
    DevicesModule,
    HierarchyModule,
    EventRulesModule,
    MappingsModule,
    SettingsModule
  ],
  providers: [
    { provide: HOOK_NAVIGATOR_NODES, useClass: HubConnectorNavigationFactory, multi: true},
    { provide: HOOK_BREADCRUMB, useClass: HubConnectorBreadcrumbFactory, multi: true},
    { provide: LOCALE_ID, useValue: 'de'}
  ],
  bootstrap: [BootstrapComponent]
})
export class AppModule {}
