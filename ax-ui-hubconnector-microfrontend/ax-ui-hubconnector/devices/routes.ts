import { DevicesListComponent } from "./devices-list.component";
import { DevicesDetailComponent } from "./devices-detail.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'devices',
        component: DevicesListComponent
    },
    {
        path: 'devices/:id',
        component: DevicesDetailComponent
    }
];