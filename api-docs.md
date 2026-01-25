{
  "openapi": "3.0.1",
  "info": {
    "title": "iFit API",
    "description": "API REST para el sistema de gestión de fitness iFit",
    "contact": {
      "name": "Juan García",
      "email": "ifit.communication@gmail.com"
    },
    "license": {
      "name": "MIT License",
      "url": "https://opensource.org/licenses/MIT"
    },
    "version": "v1.0"
  },
  "servers": [
    {
      "url": "http://localhost:8081",
      "description": "Servicio directo"
    }
  ],
  "tags": [
    {
      "name": "Experience Levels",
      "description": "Operaciones relacionadas con los niveles de experiencia"
    },
    {
      "name": "Questionnaires",
      "description": "API para gestión de cuestionarios y respuestas de usuarios"
    },
    {
      "name": "Users",
      "description": "API para gestión de usuarios"
    },
    {
      "name": "Authentication",
      "description": "Endpoints para autenticación y gestión de tokens JWT"
    },
    {
      "name": "Coach Models",
      "description": "Gestión de tipos de modelo de coach de IA"
    }
  ],
  "paths": {
    "/users/{id}": {
      "get": {
        "tags": [
          "Users"
        ],
        "summary": "Obtener usuario por ID",
        "description": "Retorna los datos de un usuario específico identificado por su ID",
        "operationId": "getUserById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "ID del usuario",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "Usuario encontrado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "404": {
            "description": "Usuario no encontrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "Users"
        ],
        "summary": "Actualizar usuario",
        "description": "Actualiza los datos de un usuario existente. Soporta actualizaciones parciales.",
        "operationId": "updateUser",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "ID del usuario a actualizar",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UpdateAppUserRequestDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Usuario actualizado exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "404": {
            "description": "Usuario no encontrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "409": {
            "description": "El nuevo email ya está en uso por otro usuario",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "Users"
        ],
        "summary": "Eliminar usuario",
        "description": "Elimina permanentemente un usuario del sistema. Esta operación es irreversible.",
        "operationId": "deleteUser",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "ID del usuario a eliminar",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "204": {
            "description": "Usuario eliminado exitosamente"
          },
          "404": {
            "description": "Usuario no encontrado"
          }
        }
      }
    },
    "/questionnaires/{id}": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Obtener cuestionario por ID",
        "description": "Recupera un cuestionario específico con toda su información.",
        "operationId": "getQuestionnaireById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Cuestionario encontrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireDTO"
                }
              }
            }
          },
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "Cuestionario no encontrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Actualizar cuestionario",
        "description": "Actualiza un cuestionario existente. Todos los campos son opcionales (actualización parcial).",
        "operationId": "updateQuestionnaire",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UpdateQuestionnaireRequestDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Cuestionario actualizado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireDTO"
                }
              }
            }
          },
          "404": {
            "description": "Cuestionario, coach, nivel o pregunta no encontrados",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "400": {
            "description": "Datos inválidos",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Eliminar cuestionario",
        "description": "Elimina un cuestionario del sistema. ADVERTENCIA: Esta acción no se puede deshacer.",
        "operationId": "deleteQuestionnaire",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "Cuestionario no encontrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "204": {
            "description": "Cuestionario eliminado exitosamente"
          }
        }
      }
    },
    "/coach-models/{id}": {
      "get": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Obtener modelo por ID",
        "description": "Obtiene la información detallada de un tipo de modelo de coach específico",
        "operationId": "getById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Modelo encontrado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          },
          "404": {
            "description": "Modelo no encontrado con el ID proporcionado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Actualizar modelo",
        "description": "Actualiza un tipo de modelo de coach existente. Permite actualización parcial. Requiere rol de administrador.",
        "operationId": "update",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UpdateCoachModelTypeRequestDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "404": {
            "description": "Modelo no encontrado con el ID proporcionado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Modelo actualizado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          },
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "403": {
            "description": "Acceso denegado - Requiere rol ADMIN",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "400": {
            "description": "Datos inválidos en la actualización",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Deshabilitar modelo",
        "description": "Deshabilita un tipo de modelo de coach (soft delete). El modelo no se elimina, solo se marca como no disponible. Requiere rol de administrador.",
        "operationId": "delete",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "404": {
            "description": "Modelo no encontrado con el ID proporcionado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "204": {
            "description": "Modelo deshabilitado exitosamente"
          },
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "403": {
            "description": "Acceso denegado - Requiere rol ADMIN",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/coach-models/{id}/enable": {
      "put": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Habilitar modelo",
        "description": "Habilita un tipo de modelo de coach previamente deshabilitado. Requiere rol de administrador.",
        "operationId": "enable",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Modelo habilitado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          },
          "404": {
            "description": "Modelo no encontrado con el ID proporcionado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "403": {
            "description": "Acceso denegado - Requiere rol ADMIN",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/auth/user/update/{userId}": {
      "put": {
        "tags": [
          "keycloak-controller"
        ],
        "operationId": "updateUser_1",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    },
    "/users": {
      "get": {
        "tags": [
          "Users"
        ],
        "summary": "Obtener todos los usuarios",
        "description": "Retorna una lista completa de todos los usuarios registrados en el sistema. Para grandes volúmenes de datos, se recomienda usar el endpoint paginado.",
        "operationId": "getAllUsers",
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista de usuarios obtenida exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "Users"
        ],
        "summary": "Crear nuevo usuario",
        "description": "Crea un nuevo usuario en el sistema con los datos proporcionados. El email debe ser único y la contraseña será encriptada automáticamente.",
        "operationId": "createUser",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CreateAppUserRequestDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "400": {
            "description": "Datos de entrada inválidos",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "409": {
            "description": "El email ya está registrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "201": {
            "description": "Usuario creado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Listar cuestionarios",
        "description": "Obtiene todos los cuestionarios habilitados en formato compacto (ideal para listados).",
        "operationId": "getAllQuestionnaires",
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista de cuestionarios obtenida exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireSummaryDto"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Crear cuestionario",
        "description": "Crea un nuevo cuestionario. Los campos coach, experienceLevel y firstQuestion son opcionales.",
        "operationId": "createQuestionnaire",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CreateQuestionnaireRequestDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "Coach, nivel de experiencia o pregunta no encontrados",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "400": {
            "description": "Datos inválidos",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "201": {
            "description": "Cuestionario creado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireDTO"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/{userId}/start/{questionnaireId}": {
      "post": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Iniciar sesión de cuestionario",
        "description": "Crea una nueva sesión de cuestionario para el usuario autenticado y devuelve la primera pregunta.",
        "operationId": "startQuestionnaire",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          },
          {
            "name": "questionnaireId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "Cuestionario no encontrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "201": {
            "description": "Sesión de cuestionario iniciada",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireResponseDTO"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/responses/{responseId}/answer": {
      "post": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Responder pregunta",
        "description": "Registra la respuesta del usuario a una pregunta y devuelve la siguiente pregunta según el árbol de decisión.",
        "operationId": "answerQuestion",
        "parameters": [
          {
            "name": "responseId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/AnswerRequestDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Respuesta registrada exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireResponseDTO"
                }
              }
            }
          },
          "400": {
            "description": "Datos inválidos o cuestionario ya completado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "Sesión, pregunta u opción no encontrada",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/experience-levels": {
      "get": {
        "tags": [
          "Experience Levels"
        ],
        "summary": "Obtiene todos los niveles de experiencia",
        "description": "Obtiene todos los niveles de experiencia",
        "operationId": "getAll",
        "responses": {
          "404": {
            "description": "No se encontraron niveles de experiencia",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/ExperienceLevelDto"
                  }
                }
              }
            }
          },
          "200": {
            "description": "Niveles de experiencia obtenidos correctamente",
            "content": {
              "*/*": {
                "schema": {
                  "type": "array",
                  "items": {
                    "$ref": "#/components/schemas/ExperienceLevelDto"
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "Experience Levels"
        ],
        "summary": "Crea un nuevo nivel de experiencia",
        "description": "Crea un nuevo nivel de experiencia con los datos proporcionados",
        "operationId": "create",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CreateExperienceLevelDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Nivel de experiencia creado correctamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          },
          "400": {
            "description": "Datos inválidos para crear el nivel de experiencia",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          }
        }
      }
    },
    "/coach-models": {
      "get": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Listar modelos habilitados",
        "description": "Obtiene todos los tipos de modelo de coach de IA que están actualmente disponibles y habilitados en el sistema",
        "operationId": "getAllEnabled",
        "responses": {
          "200": {
            "description": "Lista de modelos obtenida exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          },
          "401": {
            "description": "No autenticado - Token JWT inválido o ausente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Crear nuevo modelo",
        "description": "Crea un nuevo tipo de modelo de coach de IA. Requiere rol de administrador.",
        "operationId": "create_1",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CreateCoachModelTypeRequestDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "400": {
            "description": "Datos inválidos o nombre de modelo duplicado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "403": {
            "description": "Acceso denegado - Requiere rol ADMIN",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "201": {
            "description": "Modelo creado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/auth/user/create": {
      "post": {
        "tags": [
          "keycloak-controller"
        ],
        "operationId": "createUser_1",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    },
    "/auth/register": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Registro de nuevo usuario",
        "description": "Crea un nuevo usuario en Keycloak y en la base de datos, luego realiza login automático. Si falla, hace rollback completo.",
        "operationId": "register",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RegisterRequestDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "201": {
            "description": "Usuario creado exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "400": {
            "description": "Datos de entrada inválidos",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "409": {
            "description": "El email ya está registrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          }
        }
      }
    },
    "/auth/refresh": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Refrescar tokens JWT",
        "description": "Obtiene nuevos access token y refresh token usando un refresh token válido. NO requiere email ni password. Más rápido que login porque no consulta la base de datos. Usar cuando el access token expira (típicamente cada 5-15 minutos).",
        "operationId": "refreshToken",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RefreshTokenRequestDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "200": {
            "description": "Tokens refrescados exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "400": {
            "description": "Refresh token no proporcionado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "401": {
            "description": "Refresh token inválido o expirado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          }
        }
      }
    },
    "/auth/logout": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Cerrar sesión",
        "description": "Invalida el refresh token en Keycloak para cerrar la sesión del usuario. El cliente debe eliminar los tokens de su almacenamiento local después de llamar a este endpoint.",
        "operationId": "logout",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RefreshTokenRequestDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "500": {
            "description": "Error al comunicarse con Keycloak",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LogoutResponseDTO"
                }
              }
            }
          },
          "200": {
            "description": "Sesión cerrada exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LogoutResponseDTO"
                }
              }
            }
          },
          "400": {
            "description": "Refresh token no proporcionado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LogoutResponseDTO"
                }
              }
            }
          }
        }
      }
    },
    "/auth/login": {
      "post": {
        "tags": [
          "Authentication"
        ],
        "summary": "Login de usuario",
        "description": "Autentica un usuario con email y contraseña. Retorna access token (5 min), refresh token (30 días) y perfil completo del usuario.",
        "operationId": "login",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/LoginRequestDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "401": {
            "description": "Credenciales inválidas",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "200": {
            "description": "Login exitoso",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          },
          "400": {
            "description": "Datos de entrada inválidos",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/LoginResponseDTO"
                }
              }
            }
          }
        }
      }
    },
    "/users/{userId}/complete-registration": {
      "patch": {
        "tags": [
          "Users"
        ],
        "summary": "Completar registro de usuario",
        "description": "Marca el proceso de registro/onboarding del usuario como completado",
        "operationId": "markRegistrationComplete",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "description": "ID del usuario",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "404": {
            "description": "Usuario no encontrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "200": {
            "description": "Registro marcado como completo exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/users/{userId}/assign-experience/{levelId}": {
      "patch": {
        "tags": [
          "Users"
        ],
        "summary": "Asignar nivel de experiencia",
        "description": "Asigna un nivel de experiencia al usuario para personalizar entrenamientos",
        "operationId": "setExperienceLevel",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "description": "ID del usuario",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          },
          {
            "name": "levelId",
            "in": "path",
            "description": "ID del nivel de experiencia",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 2
          }
        ],
        "responses": {
          "404": {
            "description": "Usuario o nivel de experiencia no encontrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "200": {
            "description": "Nivel de experiencia asignado exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/users/{userId}/assign-coach/{coachId}": {
      "patch": {
        "tags": [
          "Users"
        ],
        "summary": "Asignar tipo de coach",
        "description": "Asigna un modelo de coach de IA al usuario para personalizar su experiencia",
        "operationId": "setCoachModelType",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "description": "ID del usuario",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          },
          {
            "name": "coachId",
            "in": "path",
            "description": "ID del tipo de coach",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "404": {
            "description": "Usuario o tipo de coach no encontrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "200": {
            "description": "Coach asignado exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/experience-levels/{id}": {
      "get": {
        "tags": [
          "Experience Levels"
        ],
        "summary": "Obtiene un nivel de experiencia por su ID",
        "description": "Obtiene un nivel de experiencia por su ID",
        "operationId": "getById_1",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "ID del nivel de experiencia",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "Nivel de experiencia obtenido correctamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          },
          "404": {
            "description": "Nivel de experiencia no encontrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "Experience Levels"
        ],
        "summary": "Elimina un nivel de experiencia por su ID",
        "description": "Elimina un nivel de experiencia por su ID",
        "operationId": "delete_1",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "ID del nivel de experiencia",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "Nivel de experiencia eliminado correctamente"
          },
          "404": {
            "description": "Nivel de experiencia no encontrado"
          }
        }
      },
      "patch": {
        "tags": [
          "Experience Levels"
        ],
        "summary": "Actualiza un nivel de experiencia por su ID",
        "description": "Actualiza un nivel de experiencia por su ID con los datos proporcionados",
        "operationId": "update_1",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "ID del nivel de experiencia",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UpdateExperienceLevelDto"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "Nivel de experiencia actualizado correctamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          },
          "404": {
            "description": "Nivel de experiencia no encontrado",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          }
        }
      }
    },
    "/users/paginated": {
      "get": {
        "tags": [
          "Users"
        ],
        "summary": "Obtener usuarios paginados",
        "description": "Retorna una página de usuarios con soporte para ordenamiento. Útil para grandes conjuntos de datos.",
        "operationId": "getUsersPaginated",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "description": "Número de página (empieza en 0)",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 0
            },
            "example": 0
          },
          {
            "name": "size",
            "in": "query",
            "description": "Tamaño de página",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 20
            },
            "example": 20
          },
          {
            "name": "sortBy",
            "in": "query",
            "description": "Campo de ordenamiento",
            "required": false,
            "schema": {
              "type": "string",
              "default": "createdAt"
            },
            "example": "createdAt"
          },
          {
            "name": "sortDir",
            "in": "query",
            "description": "Dirección de ordenamiento (asc/desc)",
            "required": false,
            "schema": {
              "type": "string",
              "default": "desc"
            },
            "example": "desc"
          }
        ],
        "responses": {
          "200": {
            "description": "Página de usuarios obtenida exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/PageAppUserResponseDto"
                }
              }
            }
          },
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/users/exists/email/{email}": {
      "get": {
        "tags": [
          "Users"
        ],
        "summary": "Verificar si existe email",
        "description": "Verifica si ya existe un usuario registrado con el email especificado",
        "operationId": "existsByEmail",
        "parameters": [
          {
            "name": "email",
            "in": "path",
            "description": "Email a verificar",
            "required": true,
            "schema": {
              "type": "string"
            },
            "example": "juan@example.com"
          }
        ],
        "responses": {
          "200": {
            "description": "Verificación completada",
            "content": {
              "*/*": {
                "schema": {
                  "type": "boolean"
                }
              }
            }
          }
        }
      }
    },
    "/users/email/{email}": {
      "get": {
        "tags": [
          "Users"
        ],
        "summary": "Obtener usuario por email",
        "description": "Retorna los datos de un usuario específico identificado por su email",
        "operationId": "getUserByEmail",
        "parameters": [
          {
            "name": "email",
            "in": "path",
            "description": "Email del usuario",
            "required": true,
            "schema": {
              "type": "string"
            },
            "example": "juan@example.com"
          }
        ],
        "responses": {
          "404": {
            "description": "Usuario no encontrado con ese email",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          },
          "200": {
            "description": "Usuario encontrado exitosamente",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/AppUserResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/{id}/with-first-question": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Obtener cuestionario con primera pregunta",
        "description": "Obtiene el cuestionario junto con su primera pregunta y opciones. Útil para reducir llamadas a la API al iniciar un cuestionario.",
        "operationId": "getQuestionnaireWithFirstQuestion",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Cuestionario con primera pregunta obtenido",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireWithFirstQuestionDto"
                }
              }
            }
          },
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "Cuestionario no encontrado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/responses/{responseId}/summary": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Obtener resumen de sesión",
        "description": "Recupera el resumen completo de una sesión de cuestionario con todas las respuestas del usuario.",
        "operationId": "getResponseSummary",
        "parameters": [
          {
            "name": "responseId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "404": {
            "description": "Sesión no encontrada",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Resumen obtenido exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireResponseSummaryDTO"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/responses/my-responses": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Obtener mis sesiones",
        "description": "Obtiene todas las sesiones de cuestionarios del usuario autenticado (completadas y activas).",
        "operationId": "getMyResponses",
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista de sesiones obtenida",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireResponse"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/responses/my-completed-responses": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Obtener mis sesiones completadas",
        "description": "Obtiene solo las sesiones de cuestionarios completadas por el usuario.",
        "operationId": "getMyCompletedResponses",
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista de sesiones completadas obtenida",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireResponse"
                }
              }
            }
          }
        }
      }
    },
    "/questionnaires/responses/my-active-responses": {
      "get": {
        "tags": [
          "Questionnaires"
        ],
        "summary": "Obtener mis sesiones activas",
        "description": "Obtiene las sesiones de cuestionarios que el usuario ha iniciado pero no ha completado.",
        "operationId": "getMyActiveResponses",
        "responses": {
          "500": {
            "description": "Error interno del servidor",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista de sesiones activas obtenida",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/QuestionnaireResponse"
                }
              }
            }
          }
        }
      }
    },
    "/experience-levels/{name}": {
      "get": {
        "tags": [
          "Experience Levels"
        ],
        "summary": "Obtiene un nivel de experiencia por su nombre",
        "description": "Obtiene un nivel de experiencia por su nombre",
        "operationId": "getByName",
        "parameters": [
          {
            "name": "name",
            "in": "path",
            "description": "Nombre del nivel de experiencia",
            "required": true,
            "schema": {
              "type": "string"
            },
            "example": "Beginner"
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ExperienceLevelDto"
                }
              }
            }
          }
        }
      }
    },
    "/coach-models/name/{name}": {
      "get": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Buscar modelo por nombre",
        "description": "Busca un tipo de modelo de coach por su nombre exacto",
        "operationId": "getByName_1",
        "parameters": [
          {
            "name": "name",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Modelo encontrado exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          },
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "404": {
            "description": "No se encontró ningún modelo con ese nombre",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          }
        }
      }
    },
    "/coach-models/all": {
      "get": {
        "tags": [
          "Coach Models"
        ],
        "summary": "Listar todos los modelos",
        "description": "Obtiene todos los tipos de modelo de coach, incluyendo los deshabilitados. Requiere rol de administrador.",
        "operationId": "getAll_1",
        "responses": {
          "401": {
            "description": "No autenticado",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "403": {
            "description": "Acceso denegado - Requiere rol ADMIN",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ErrorResponse"
                }
              }
            }
          },
          "200": {
            "description": "Lista completa de modelos obtenida exitosamente",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/CoachModelTypeResponseDto"
                }
              }
            }
          }
        }
      }
    },
    "/auth/user/search": {
      "get": {
        "tags": [
          "keycloak-controller"
        ],
        "operationId": "findAllUsers",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    },
    "/auth/user/search/{username}": {
      "get": {
        "tags": [
          "keycloak-controller"
        ],
        "operationId": "searchUserByUsername",
        "parameters": [
          {
            "name": "username",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    },
    "/auth/user/delete/{userId}": {
      "delete": {
        "tags": [
          "keycloak-controller"
        ],
        "operationId": "deleteUser_1",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "type": "object"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "UpdateAppUserRequestDto": {
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 50,
            "minLength": 3,
            "type": "string",
            "description": "Nuevo nombre del usuario",
            "example": "Juan García Pérez"
          },
          "email": {
            "type": "string",
            "description": "Nuevo email del usuario",
            "example": "nuevoemail@example.com"
          }
        },
        "description": "Datos a actualizar"
      },
      "AppUserResponseDto": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "ID único del usuario",
            "format": "int64",
            "example": 1
          },
          "name": {
            "type": "string",
            "description": "Nombre completo del usuario",
            "example": "Juan García"
          },
          "email": {
            "type": "string",
            "description": "Email del usuario",
            "example": "juan@example.com"
          },
          "createdAt": {
            "type": "string",
            "description": "Fecha de creación del usuario",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "Fecha de última actualización",
            "format": "date-time"
          },
          "roleName": {
            "type": "string",
            "description": "Rol del usuario",
            "example": "USER"
          },
          "coachModelTypeName": {
            "type": "string",
            "description": "Tipo de modelo de coach asignado",
            "nullable": true,
            "example": "GPT-4"
          },
          "experienceLevelName": {
            "type": "string",
            "description": "Nivel de experiencia del usuario",
            "nullable": true,
            "example": "INTERMEDIATE"
          },
          "verified": {
            "type": "boolean"
          },
          "registrationComplete": {
            "type": "boolean"
          }
        },
        "description": "Información completa de un usuario"
      },
      "UpdateQuestionnaireRequestDto": {
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 100,
            "minLength": 3,
            "type": "string",
            "description": "Nuevo nombre del cuestionario (opcional)",
            "nullable": true,
            "example": "Ronnie - Fuerza Mejorado"
          },
          "description": {
            "maxLength": 1000,
            "minLength": 10,
            "type": "string",
            "description": "Nueva descripción (opcional)",
            "nullable": true,
            "example": "Descripción actualizada del programa..."
          },
          "coachModelTypeId": {
            "type": "integer",
            "description": "Nuevo ID de coach (opcional, null para quitar asignación)",
            "format": "int64",
            "nullable": true,
            "example": 3
          },
          "experienceLevelId": {
            "type": "integer",
            "description": "Nuevo ID de nivel de experiencia (opcional, null para quitar asignación)",
            "format": "int64",
            "nullable": true,
            "example": 2
          },
          "firstQuestionId": {
            "type": "integer",
            "description": "Nuevo ID de primera pregunta (opcional)",
            "format": "int64",
            "nullable": true,
            "example": 5
          },
          "isEnabled": {
            "type": "boolean",
            "description": "Nuevo estado de habilitación (opcional)",
            "nullable": true,
            "example": false
          }
        },
        "description": "Datos para actualizar un cuestionario existente"
      },
      "QuestionnaireDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "ID único del cuestionario",
            "format": "int64",
            "example": 1
          },
          "name": {
            "type": "string",
            "description": "Nombre del cuestionario",
            "example": "Ronnie - Fuerza para Principiantes"
          },
          "description": {
            "type": "string",
            "description": "Descripción detallada del cuestionario",
            "example": "Programa intenso de fuerza diseñado por Ronnie..."
          },
          "coachModelTypeName": {
            "type": "string",
            "description": "Nombre del coach asociado",
            "nullable": true,
            "example": "Ronnie"
          },
          "coachModelTypeEmoji": {
            "type": "string",
            "description": "Emoji del coach",
            "nullable": true,
            "example": "💪"
          },
          "experienceLevelName": {
            "type": "string",
            "description": "Nivel de experiencia recomendado",
            "nullable": true,
            "example": "Principiante"
          },
          "firstQuestionId": {
            "type": "integer",
            "description": "ID de la primera pregunta",
            "format": "int64",
            "nullable": true,
            "example": 1
          },
          "isEnabled": {
            "type": "boolean",
            "description": "Indica si el cuestionario está habilitado",
            "example": true
          },
          "createdAt": {
            "type": "string",
            "description": "Fecha de creación del cuestionario",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "Fecha de última actualización",
            "format": "date-time"
          }
        },
        "description": "Información completa de un cuestionario"
      },
      "ErrorResponse": {
        "type": "object",
        "properties": {
          "error": {
            "type": "string"
          },
          "status": {
            "type": "integer",
            "format": "int32"
          },
          "timestamp": {
            "type": "string"
          },
          "message": {
            "type": "string"
          }
        }
      },
      "UpdateCoachModelTypeRequestDto": {
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 100,
            "minLength": 2,
            "type": "string",
            "description": "Nombre del modelo de coach",
            "example": "GPT-4 Turbo"
          },
          "description": {
            "maxLength": 500,
            "minLength": 0,
            "type": "string",
            "description": "Descripción detallada del modelo",
            "example": "Versión mejorada con mayor velocidad y capacidad de contexto"
          },
          "emojiCharacter": {
            "maxLength": 10,
            "minLength": 0,
            "type": "string",
            "description": "Emoji representativo del modelo",
            "example": "⚡"
          },
          "enabled": {
            "type": "boolean",
            "description": "Indica si el modelo debe estar habilitado",
            "example": false
          }
        },
        "description": "Datos para actualizar un tipo de modelo de coach existente"
      },
      "CoachModelTypeResponseDto": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "ID único del tipo de modelo",
            "format": "int64",
            "example": 1
          },
          "name": {
            "type": "string",
            "description": "Nombre del modelo de coach",
            "example": "GPT-4"
          },
          "description": {
            "type": "string",
            "description": "Descripción detallada del modelo",
            "example": "Modelo de lenguaje avanzado con capacidades de razonamiento profundo"
          },
          "emojiCharacter": {
            "type": "string",
            "description": "Emoji representativo del modelo",
            "example": "🤖"
          },
          "enabled": {
            "type": "boolean",
            "description": "Indica si el modelo está habilitado",
            "example": true
          },
          "createdAt": {
            "type": "string",
            "description": "Fecha de creación del registro",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "Fecha de última actualización",
            "format": "date-time",
            "nullable": true
          }
        },
        "description": "Información completa de un tipo de modelo de coach"
      },
      "UserDTO": {
        "type": "object",
        "properties": {
          "username": {
            "type": "string"
          },
          "email": {
            "type": "string"
          },
          "firstName": {
            "type": "string"
          },
          "lastName": {
            "type": "string"
          },
          "password": {
            "type": "string"
          },
          "roles": {
            "uniqueItems": true,
            "type": "array",
            "items": {
              "type": "string"
            }
          }
        }
      },
      "CreateAppUserRequestDto": {
        "required": [
          "email",
          "name",
          "password"
        ],
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 50,
            "minLength": 3,
            "type": "string",
            "description": "Nombre completo del usuario",
            "example": "Juan García"
          },
          "password": {
            "maxLength": 100,
            "minLength": 8,
            "type": "string",
            "description": "Contraseña del usuario",
            "example": "SecurePass123!"
          },
          "email": {
            "type": "string",
            "description": "Email del usuario",
            "example": "juan@example.com"
          },
          "keycloakId": {
            "type": "string",
            "description": "Keycloak ID del usuario",
            "example": "123e4567-e89b-12d3-a456-426614174000"
          }
        },
        "description": "Datos del nuevo usuario"
      },
      "CreateQuestionnaireRequestDto": {
        "required": [
          "description",
          "name"
        ],
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 100,
            "minLength": 3,
            "type": "string",
            "description": "Nombre único del cuestionario",
            "example": "Ronnie - Fuerza para Principiantes"
          },
          "description": {
            "maxLength": 1000,
            "minLength": 10,
            "type": "string",
            "description": "Descripción detallada del cuestionario",
            "example": "Programa intenso de fuerza diseñado por Ronnie. Ideal para quienes quieren construir músculo desde cero."
          },
          "coachModelTypeId": {
            "type": "integer",
            "description": "ID del coach asociado (opcional)",
            "format": "int64",
            "nullable": true,
            "example": 2
          },
          "experienceLevelId": {
            "type": "integer",
            "description": "ID del nivel de experiencia recomendado (opcional)",
            "format": "int64",
            "nullable": true,
            "example": 1
          },
          "firstQuestionId": {
            "type": "integer",
            "description": "ID de la primera pregunta del árbol (opcional, se puede asignar después)",
            "format": "int64",
            "nullable": true,
            "example": 1
          }
        },
        "description": "Datos para crear un nuevo cuestionario"
      },
      "OptionDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "text": {
            "type": "string"
          },
          "requiresTextInput": {
            "type": "boolean"
          },
          "textInputPrompt": {
            "type": "string"
          },
          "textInputPlaceholder": {
            "type": "string"
          }
        }
      },
      "QuestionDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "text": {
            "type": "string"
          },
          "type": {
            "type": "string",
            "enum": [
              "BINARY",
              "MULTIPLE_CHOICE",
              "TEXT_INPUT",
              "NUMERIC",
              "SCALE"
            ]
          },
          "options": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/OptionDTO"
            }
          }
        },
        "description": "Primera pregunta del cuestionario con todas sus opciones"
      },
      "QuestionnaireResponseDTO": {
        "type": "object",
        "properties": {
          "responseId": {
            "type": "integer",
            "format": "int64"
          },
          "currentQuestion": {
            "$ref": "#/components/schemas/QuestionDTO"
          },
          "isCompleted": {
            "type": "boolean"
          },
          "totalQuestionsAnswered": {
            "type": "integer",
            "format": "int32"
          }
        }
      },
      "AnswerRequestDTO": {
        "required": [
          "questionId",
          "selectedOptionId"
        ],
        "type": "object",
        "properties": {
          "questionId": {
            "type": "integer",
            "format": "int64"
          },
          "selectedOptionId": {
            "type": "integer",
            "format": "int64"
          },
          "additionalText": {
            "type": "string"
          }
        }
      },
      "CreateExperienceLevelDto": {
        "required": [
          "description",
          "name"
        ],
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 30,
            "minLength": 3,
            "type": "string",
            "description": "Nombre del nivel de experiencia",
            "example": "Principiante"
          },
          "description": {
            "type": "string",
            "description": "Descripción del nivel de experiencia",
            "example": "Nivel para personas que están empezando a entrenar"
          }
        },
        "description": "Nivel de experiencia a crear"
      },
      "ExperienceLevelDto": {
        "required": [
          "id",
          "name"
        ],
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "maxLength": 30,
            "minLength": 5,
            "type": "string"
          },
          "description": {
            "type": "string"
          }
        }
      },
      "CreateCoachModelTypeRequestDto": {
        "required": [
          "enabled",
          "name"
        ],
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 100,
            "minLength": 2,
            "type": "string",
            "description": "Nombre del modelo de coach",
            "example": "GPT-4"
          },
          "description": {
            "maxLength": 500,
            "minLength": 0,
            "type": "string",
            "description": "Descripción detallada del modelo",
            "example": "Modelo de lenguaje avanzado con capacidades de razonamiento profundo"
          },
          "emojiCharacter": {
            "maxLength": 10,
            "minLength": 0,
            "type": "string",
            "description": "Emoji representativo del modelo",
            "example": "🤖"
          },
          "enabled": {
            "type": "boolean",
            "description": "Indica si el modelo debe estar habilitado inicialmente",
            "example": true,
            "default": true
          }
        },
        "description": "Datos para crear un nuevo tipo de modelo de coach"
      },
      "RegisterRequestDTO": {
        "required": [
          "email",
          "name",
          "password"
        ],
        "type": "object",
        "properties": {
          "name": {
            "maxLength": 50,
            "minLength": 2,
            "type": "string"
          },
          "surname": {
            "maxLength": 50,
            "minLength": 2,
            "type": "string"
          },
          "email": {
            "type": "string"
          },
          "password": {
            "maxLength": 2147483647,
            "minLength": 8,
            "type": "string"
          },
          "birthDate": {
            "type": "string",
            "format": "date"
          },
          "phone": {
            "type": "string"
          }
        }
      },
      "LoginResponseDTO": {
        "type": "object",
        "properties": {
          "accessToken": {
            "type": "string"
          },
          "refreshToken": {
            "type": "string"
          },
          "expiresIn": {
            "type": "integer",
            "format": "int32"
          },
          "tokenType": {
            "type": "string"
          },
          "appUser": {
            "$ref": "#/components/schemas/AppUserResponseDto"
          }
        }
      },
      "RefreshTokenRequestDTO": {
        "required": [
          "refreshToken"
        ],
        "type": "object",
        "properties": {
          "refreshToken": {
            "type": "string"
          }
        }
      },
      "LogoutResponseDTO": {
        "type": "object",
        "properties": {
          "message": {
            "type": "string"
          },
          "success": {
            "type": "boolean"
          }
        }
      },
      "LoginRequestDTO": {
        "type": "object",
        "properties": {
          "username": {
            "type": "string"
          },
          "password": {
            "type": "string"
          }
        }
      },
      "UpdateExperienceLevelDto": {
        "required": [
          "description"
        ],
        "type": "object",
        "properties": {
          "description": {
            "type": "string",
            "description": "Descripción del nivel de experiencia",
            "example": "Nivel para personas que están empezando a entrenar"
          }
        },
        "description": "Datos para actualizar el nivel de experiencia"
      },
      "PageAppUserResponseDto": {
        "type": "object",
        "properties": {
          "totalElements": {
            "type": "integer",
            "format": "int64"
          },
          "totalPages": {
            "type": "integer",
            "format": "int32"
          },
          "first": {
            "type": "boolean"
          },
          "last": {
            "type": "boolean"
          },
          "size": {
            "type": "integer",
            "format": "int32"
          },
          "content": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/AppUserResponseDto"
            }
          },
          "number": {
            "type": "integer",
            "format": "int32"
          },
          "sort": {
            "$ref": "#/components/schemas/SortObject"
          },
          "numberOfElements": {
            "type": "integer",
            "format": "int32"
          },
          "pageable": {
            "$ref": "#/components/schemas/PageableObject"
          },
          "empty": {
            "type": "boolean"
          }
        }
      },
      "PageableObject": {
        "type": "object",
        "properties": {
          "offset": {
            "type": "integer",
            "format": "int64"
          },
          "sort": {
            "$ref": "#/components/schemas/SortObject"
          },
          "pageNumber": {
            "type": "integer",
            "format": "int32"
          },
          "pageSize": {
            "type": "integer",
            "format": "int32"
          },
          "paged": {
            "type": "boolean"
          },
          "unpaged": {
            "type": "boolean"
          }
        }
      },
      "SortObject": {
        "type": "object",
        "properties": {
          "empty": {
            "type": "boolean"
          },
          "sorted": {
            "type": "boolean"
          },
          "unsorted": {
            "type": "boolean"
          }
        }
      },
      "QuestionnaireSummaryDto": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "ID del cuestionario",
            "format": "int64",
            "example": 1
          },
          "name": {
            "type": "string",
            "description": "Nombre del cuestionario",
            "example": "Ronnie - Fuerza para Principiantes"
          },
          "description": {
            "type": "string",
            "description": "Descripción breve",
            "example": "Programa intenso de fuerza..."
          },
          "coachModelTypeName": {
            "type": "string",
            "description": "Coach asignado",
            "nullable": true,
            "example": "Ronnie"
          },
          "coachModelTypeEmoji": {
            "type": "string",
            "description": "Emoji del coach",
            "nullable": true,
            "example": "💪"
          },
          "experienceLevelName": {
            "type": "string",
            "description": "Nivel recomendado",
            "nullable": true,
            "example": "Principiante"
          },
          "isEnabled": {
            "type": "boolean",
            "description": "Estado del cuestionario",
            "example": true
          }
        },
        "description": "Resumen compacto de un cuestionario"
      },
      "QuestionnaireWithFirstQuestionDto": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "ID del cuestionario",
            "format": "int64",
            "example": 1
          },
          "name": {
            "type": "string",
            "description": "Nombre del cuestionario",
            "example": "Ronnie - Fuerza para Principiantes"
          },
          "description": {
            "type": "string",
            "description": "Descripción del cuestionario"
          },
          "coachModelTypeName": {
            "type": "string",
            "description": "Coach asociado",
            "nullable": true,
            "example": "Ronnie"
          },
          "coachModelTypeEmoji": {
            "type": "string",
            "description": "Emoji del coach",
            "nullable": true,
            "example": "💪"
          },
          "experienceLevelName": {
            "type": "string",
            "description": "Nivel recomendado",
            "nullable": true,
            "example": "Principiante"
          },
          "isEnabled": {
            "type": "boolean",
            "description": "Estado del cuestionario",
            "example": true
          },
          "createdAt": {
            "type": "string",
            "description": "Fecha de creación",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "Fecha de actualización",
            "format": "date-time"
          },
          "firstQuestion": {
            "$ref": "#/components/schemas/QuestionDTO"
          }
        },
        "description": "Cuestionario con su primera pregunta incluida (para iniciar)"
      },
      "AnswerDTO": {
        "type": "object",
        "properties": {
          "answerId": {
            "type": "integer",
            "format": "int64"
          },
          "questionText": {
            "type": "string"
          },
          "selectedOption": {
            "type": "string"
          },
          "additionalText": {
            "type": "string"
          },
          "aiDescription": {
            "type": "string"
          },
          "answeredAt": {
            "type": "string",
            "format": "date-time"
          }
        }
      },
      "QuestionnaireResponseSummaryDTO": {
        "type": "object",
        "properties": {
          "responseId": {
            "type": "integer",
            "format": "int64"
          },
          "userId": {
            "type": "integer",
            "format": "int64"
          },
          "userName": {
            "type": "string"
          },
          "questionnaireId": {
            "type": "integer",
            "format": "int64"
          },
          "questionnaireName": {
            "type": "string"
          },
          "questionnaireDescription": {
            "type": "string"
          },
          "answers": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/AnswerDTO"
            }
          },
          "startedAt": {
            "type": "string",
            "format": "date-time"
          },
          "completedAt": {
            "type": "string",
            "format": "date-time"
          },
          "isCompleted": {
            "type": "boolean"
          }
        }
      },
      "AppRole": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          }
        }
      },
      "AppUser": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "password": {
            "type": "string"
          },
          "email": {
            "type": "string"
          },
          "isRegistrationComplete": {
            "type": "boolean",
            "writeOnly": true
          },
          "keycloakId": {
            "type": "string"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "format": "date-time"
          },
          "verificationCode": {
            "type": "string"
          },
          "verificationCodeExpiresAt": {
            "type": "string",
            "format": "date-time"
          },
          "role": {
            "$ref": "#/components/schemas/AppRole"
          },
          "coachModelType": {
            "$ref": "#/components/schemas/CoachModelType"
          },
          "experienceLevel": {
            "$ref": "#/components/schemas/ExperienceLevel"
          },
          "verified": {
            "type": "boolean"
          },
          "authorities": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/GrantedAuthority"
            }
          },
          "registrationComplete": {
            "type": "boolean"
          }
        }
      },
      "CoachModelType": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "description": {
            "type": "string"
          },
          "emojiCharacter": {
            "type": "string"
          },
          "enabled": {
            "type": "boolean"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "format": "date-time"
          }
        }
      },
      "ExperienceLevel": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "description": {
            "type": "string"
          }
        }
      },
      "GrantedAuthority": {
        "type": "object",
        "properties": {
          "authority": {
            "type": "string"
          }
        }
      },
      "Question": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "text": {
            "type": "string"
          },
          "type": {
            "type": "string",
            "enum": [
              "BINARY",
              "MULTIPLE_CHOICE",
              "TEXT_INPUT",
              "NUMERIC",
              "SCALE"
            ]
          },
          "options": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/QuestionOption"
            }
          },
          "isEnabled": {
            "type": "boolean"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          }
        }
      },
      "QuestionOption": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "question": {
            "$ref": "#/components/schemas/Question"
          },
          "text": {
            "type": "string"
          },
          "nextQuestion": {
            "$ref": "#/components/schemas/Question"
          },
          "displayOrder": {
            "type": "integer",
            "format": "int32"
          },
          "requiresTextInput": {
            "type": "boolean"
          },
          "textInputPrompt": {
            "type": "string"
          },
          "textInputPlaceholder": {
            "type": "string"
          }
        }
      },
      "Questionnaire": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "name": {
            "type": "string"
          },
          "description": {
            "type": "string"
          },
          "coachModelType": {
            "$ref": "#/components/schemas/CoachModelType"
          },
          "experienceLevel": {
            "$ref": "#/components/schemas/ExperienceLevel"
          },
          "firstQuestion": {
            "$ref": "#/components/schemas/Question"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "format": "date-time"
          },
          "isEnabled": {
            "type": "boolean"
          }
        }
      },
      "QuestionnaireResponse": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "user": {
            "$ref": "#/components/schemas/AppUser"
          },
          "questionnaire": {
            "$ref": "#/components/schemas/Questionnaire"
          },
          "answers": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/UserAnswer"
            }
          },
          "startedAt": {
            "type": "string",
            "format": "date-time"
          },
          "completedAt": {
            "type": "string",
            "format": "date-time"
          },
          "isCompleted": {
            "type": "boolean"
          },
          "isActive": {
            "type": "boolean"
          }
        }
      },
      "UserAnswer": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "response": {
            "$ref": "#/components/schemas/QuestionnaireResponse"
          },
          "question": {
            "$ref": "#/components/schemas/Question"
          },
          "selectedOption": {
            "$ref": "#/components/schemas/QuestionOption"
          },
          "additionalText": {
            "type": "string"
          },
          "aiGeneratedDescription": {
            "type": "string"
          },
          "answeredAt": {
            "type": "string",
            "format": "date-time"
          }
        }
      }
    }
  }
}