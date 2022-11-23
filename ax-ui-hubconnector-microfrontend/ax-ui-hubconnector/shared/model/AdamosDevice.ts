export type AdamosDevice = {
    uuid: string;
    customerIdentification: {
        description?: string;
        inventoryNumber?: number;
        name: string;
    },
    manufacturerId: string;
    version: number
}

export type AdamosHubDevice = AdamosDevice & {
    linked: boolean;
}