# ms-rutaexpress-mensajeria

Microservicio de mensajería de RutaExpress para Desarrollo Cloud Native I, evaluación 2.

## Estado de esta rama

Estás viendo **main**, que conserva la base inicial de este repositorio. No contiene la implementación Java de EV2 ni es una entrega EV1. Esta actualización añade únicamente documentación en español.

La implementación se encuentra en [desarrollo](https://github.com/matizk/ms-rutaexpress-mensajeria/tree/desarrollo). Consume eventos de Envíos, guarda notificaciones internas, comprobantes y auditoría en PostgreSQL y permite administrar recursos de RabbitMQ con autorización. La preparación local no acredita el acceso Azure/Duoc ni el despliegue real en AWS.

## Por dónde comenzar

- [Guía completa del equipo y pendientes](https://github.com/matizk/ms-rutaexpress-envios/blob/desarrollo/CONTINUAR_EV2_EQUIPO.md).
- [Qué instalar, cómo descargar los seis repositorios y cómo iniciar](https://github.com/matizk/ms-rutaexpress-envios/blob/desarrollo/PREPARACION_EQUIPO_EV2.md).
- [Funcionamiento técnico y rutas del servicio](https://github.com/matizk/ms-rutaexpress-mensajeria/blob/desarrollo/README.md).
- [Organización de ramas y colaboración](COLABORACION.md).

## Repositorios del sistema

Los seis componentes son públicos bajo la cuenta matizk:

- [frontend-rutaexpress](https://github.com/matizk/frontend-rutaexpress): Angular local.
- [ms-rutaexpress-bff](https://github.com/matizk/ms-rutaexpress-bff): acceso y enrutamiento (BFF).
- [ms-rutaexpress-catalogo](https://github.com/matizk/ms-rutaexpress-catalogo): servicios, tarifas y cupos.
- [ms-rutaexpress-envios](https://github.com/matizk/ms-rutaexpress-envios): solicitudes, estados y herramientas de integración.
- [ms-rutaexpress-mensajeria](https://github.com/matizk/ms-rutaexpress-mensajeria): eventos, comprobantes y administración RabbitMQ.
- [ms-rutaexpress-reportes](https://github.com/matizk/ms-rutaexpress-reportes): indicadores operacionales.

Mantener las seis carpetas como hermanas y descargar desarrollo para continuar EV2. No copiar contraseñas o tokens a GitHub. Los nombres técnicos de herramientas, rutas de API y commits históricos no se traducen ni se reescriben.
