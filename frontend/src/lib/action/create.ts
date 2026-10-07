import { CreateTicketDTO } from "../../components/form/create-ticket";

interface TicketResponseDTO {
    id: number;
    titulo: string;
    descripcion: string;
    categoria: string;
    urgencia: string;
    sentimiento: string;
    estado: string;
    fechaCreacion: string;
}

const API_URL = import.meta.env.VITE_API_URL;


export const createTicket = async (data: CreateTicketDTO): Promise<TicketResponseDTO> => {
   
    const response = await fetch(`${API_URL}/tickeds/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error(`Error al crear el ticket: ${response.status}`);
    }

    return response.json();
}