# Reglas de negocio

1. Profesional: no puede tener citas solapadas.
2. Paciente: no puede tener citas solapadas.
3. Box: no puede tener citas presenciales solapadas.
4. Profesional no disponible: no se agenda si está AUSENTE, VACACIONES o AGENDA_PAUSADA.
5. Presencial: requiere box 1..8 habilitado.
6. Online: no requiere box.
7. Stock bajo: `stock <= 3`.
8. Deserción MVP: alerta cuando días desde última sesión COMPLETADA >= 21. El umbral es configurable.
9. Los datos de demo son ficticios.
10. MEDIlink no se integra sin contrato/API oficial.
