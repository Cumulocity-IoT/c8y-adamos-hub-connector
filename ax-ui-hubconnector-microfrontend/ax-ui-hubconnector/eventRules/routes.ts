import {  EventRulesListComponent } from "./eventRules-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'hub/eventRules',
        redirectTo: 'hub/eventRules/fromAdamosHub',
        pathMatch: 'full'
    },
    {
        path: 'hub/eventRules/:direction',
        component: EventRulesListComponent
    }
];