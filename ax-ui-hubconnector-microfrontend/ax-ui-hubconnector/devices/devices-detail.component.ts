import { _, AppStateService } from '@c8y/ngx-components';
import { Component } from '@angular/core';
import { InventoryService, IManagedObject } from '@c8y/client';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import * as moment from 'moment';

/*
 * Loads the selected device to configure the HubSettings in detail.
 */
@Component({
    selector: 'device',
    templateUrl: './devices-detail.component.html'
})
export class DevicesDetailComponent {
    device: IManagedObject;
    informationText: string;
    isLoading$: BehaviorSubject<boolean> = new BehaviorSubject(false);
    lastSync: string;
    initialSync: string;

    constructor(private inventory: InventoryService,
                private route: ActivatedRoute, 
                private router: Router,
                private appStateService: AppStateService) {
        // _ annotation to mark this string as translatable string.
        moment.locale(this.appStateService.state.lang);

        this.informationText = _('Ooops! It seems that there is no device to display.');
        this.isLoading$.next(true);

        this.loadDevice().then(() => {
            this.isLoading$.next(false);
        });
    }

    // Promise-based usage of InventoryService.
    async loadDevice() {
        const id = this.route.snapshot.paramMap.get('id');
        const { data } = await this.inventory.detail(id);
        this.device = data;

        if (this.device.adamos_hub_connectorSettings != undefined) {
            this.lastSync = moment(new Date(this.device.adamos_hub_connectorSettings.lastSync)).format("lll");
            this.initialSync = moment(new Date(this.device.adamos_hub_connectorSettings.initialSync)).format("lll");
        }
    }

    // Update the current device with the new settings
    async saveDevice() {
        if (this.device != undefined) {
            this.isLoading$.next(true);
            await this.inventory.update(this.device);
            this.router.navigate(["/devices"]);
        }
    }

}