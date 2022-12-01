import { MappingsDetailComponent } from "./mappings-detail.component";
import { MappingsListComponent } from "./mappings-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'hub/mappings',
        component: MappingsListComponent
    },
    {
        path: 'hub/mappings/:id',
        component: MappingsDetailComponent
    }
];