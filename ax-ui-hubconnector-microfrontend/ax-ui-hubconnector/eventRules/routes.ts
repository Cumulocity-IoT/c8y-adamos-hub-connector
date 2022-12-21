import { EventRulesToHubComponent } from "./event-rules-to-hub.component";
import {  EventRulesListComponent } from "./eventRules-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'hub/event-rules/from-hub',
        component: EventRulesListComponent
    },
    {
        path: 'hub/event-rules/to-hub',
        component: EventRulesToHubComponent
    }
];