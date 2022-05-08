import {  EventRulesListComponent } from "./eventRules-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'eventRules',
        redirectTo: 'eventRules/fromAdamosHub',
        pathMatch: 'full'
    },
    {
        path: 'eventRules/:direction',
        component: EventRulesListComponent
    }
];