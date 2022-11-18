import { MappingsDetailComponent } from "./mappings-detail.component";
import { MappingsListComponent } from "./mappings-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'mappings',
        component: MappingsListComponent
    },
    {
        path: 'mappings/:id',
        component: MappingsDetailComponent
    }
];