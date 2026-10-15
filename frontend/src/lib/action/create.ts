import { CreateTicketDTO } from "../../components/form/create-ticket";

const API_URL = import.meta.env.VITE_API_URL;

// El backend responde todos los errores con { message, status } (GlobalExceptionController),
// así que el mensaje se puede enseñar tal cual en lugar de un "algo salió mal" genérico.
const readErrorMessage = async (response: Response): Promise<string> => {
    try {
        const body = await response.json();
        if (typeof body?.message === 'string' && body.message) {
            return body.message;
        }
    } catch {
        // Si no es JSON con esa forma, caemos en el mensaje genérico de abajo.
    }

    return `Error al crear el ticket: ${response.status}`;
}

export const createTicket = async (data: CreateTicketDTO): Promise<void> => {

    const response = await fetch(`${API_URL}/tickets/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    });

    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }
}