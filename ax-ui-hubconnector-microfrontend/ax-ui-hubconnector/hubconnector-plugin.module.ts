import { LOCALE_ID, NgModule } from "@angular/core";

import {
  CoreModule,
  HOOK_BREADCRUMB,
  HOOK_NAVIGATOR_NODES,
} from "@c8y/ngx-components";

import { FormsModule } from "@angular/forms";
import { SettingsModule } from "./settings/settings.module";
import { DevicesModule } from "./devices/devices.module";
import { MappingsModule } from "./mappings/mappings.module";
import { EventRulesModule } from "./eventRules/eventRules.module";
import { HierarchyModule } from "./hierarchy/hierarchy.module";
import { HubConnectorNavigationFactory } from "./factories/Navigation";
import { HubConnectorBreadcrumbFactory } from "./factories/Breadcrumb";

@NgModule({
  imports: [
    CoreModule,
    FormsModule,
    DevicesModule,
    HierarchyModule,
    EventRulesModule,
    MappingsModule,
    SettingsModule,
  ],
  providers: [
    { provide: HOOK_NAVIGATOR_NODES, useClass: HubConnectorNavigationFactory, multi: true},
    { provide: HOOK_BREADCRUMB, useClass: HubConnectorBreadcrumbFactory, multi: true},
    { provide: LOCALE_ID, useValue: 'de'}
  ],
})
export class HubConnectorPluginModule {}
