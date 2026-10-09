export interface TicketStatsDTO {
    total: number;
    open: number;
    high: number;
    resolved: number;
}

const API_URL = import.meta.env.VITE_API_URL;

export const getTicketStats = async (): Promise<TicketStatsDTO> => {
    const response = await fetch(`${API_URL}/tickets/stats`);

    if (!response.ok) {
        throw new Error(`Error al obtener las estadísticas: ${response.status}`);
    }

    return response.json();
}