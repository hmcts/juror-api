module "postgresql_flexible" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source                   = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env                      = var.env
  product                  = var.product
  resource_group_name      = local.rg_name
  component                = var.component
  business_area            = "sds"
  location                 = var.location
  pgsql_sku                = "GP_Standard_D4s_v3"
  pgsql_storage_mb         = var.pgsql_storage_mb
  pgsql_storage_tier       = var.pgsql_storage_tier
  service_criticality      = var.service_criticality
  manage_reader_role_on_rg = false
  enable_qpi               = var.enable_qpi

  common_tags          = var.common_tags
  admin_user_object_id = var.jenkins_AAD_objectId
  pgsql_databases = [
    {
      name : local.db_name
    }
  ]
  pgsql_server_configuration = concat([
    {
      name  = "azure.extensions"
      value = "dblink,tablefunc,pg_stat_statements"
    },
    {
      name  = "backslash_quote"
      value = "on"
    },
    {
      name  = "azure.enable_temp_tablespaces_on_local_ssd"
      value = "off"
    },
    {
      name  = "track_io_timing"
      value = "ON"
    },
  ], var.pgsql_server_configuration_extra)
  pgsql_version = "16"
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "jit" {
  server_name         = "juror-api-${var.env}"
  resource_group_name = local.rg_name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = data.azuread_group.dts_jit_access_juror_db_admin.object_id
  principal_name      = data.azuread_group.dts_jit_access_juror_db_admin.display_name
  principal_type      = "Group"

  depends_on = [module.postgresql_flexible]
}
