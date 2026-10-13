export interface TicketRow {
    id: number;
    title: string;
    description: string;
    urgency: string;   
    category: string;  
    feeling: string;   
    status: string;    
}


export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
}

export interface TicketFilters {
    search?: string;
    category?: string;
    urgency?: string;
}

const API_URL = import.meta.env.VITE_API_URL;

export const getTickets = async (
    page: number,
    size: number,
    filters: TicketFilters = {}
): Promise<PageResponse<TicketRow>> => {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
    });

    const search = filters.search?.trim();
    if (search) params.set('search', search);
    if (filters.category) params.set('category', filters.category);
    if (filters.urgency) params.set('urgency', filters.urgency);

    const response = await fetch(`${API_URL}/tickets?${params}`);

    if (!response.ok) {
        throw new Error(`Error al obtener los tickets: ${response.status}`);
    }

    return response.json();
}


export const setTicketArchived = async (id: number, archived: boolean): Promise<void> => {
    const response = await fetch(`${API_URL}/tickets/archived/${id}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ archived }),
    });

    if (!response.ok) {
        const action = archived ? 'archivar' : 'desarchivar';
        throw new Error(`Error al ${action} el ticket: ${response.status}`);
    }
};
