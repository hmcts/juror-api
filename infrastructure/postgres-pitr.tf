module "postgresql_flexible_pitr" {
  count = local.postgres_pitr_enabled ? 1 : 0

  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source              = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env                 = var.env
  product             = var.product
  component           = var.component
  name                = var.postgres_pitr.name
  resource_group_name = local.rg_name
  business_area       = "sds"
  location            = var.location

  create_mode      = "PointInTimeRestore"
  restore_time     = var.postgres_pitr.restore_time
  source_server_id = module.postgresql_flexible.instance_id

  pgsql_version            = "16"
  pgsql_sku                = "GP_Standard_D4s_v3"
  pgsql_storage_mb         = var.pgsql_storage_mb
  pgsql_storage_tier       = var.pgsql_storage_tier
  service_criticality      = var.service_criticality
  manage_reader_role_on_rg = false
  enable_qpi               = var.enable_qpi
  public_access            = false
  high_availability        = false

  common_tags          = var.common_tags
  admin_user_object_id = var.jenkins_AAD_objectId

  pgsql_databases = []

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
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "jit_pitr" {
  count = local.postgres_pitr_enabled ? 1 : 0

  server_name         = "${var.postgres_pitr.name}-${var.env}"
  resource_group_name = local.rg_name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = data.azuread_group.dts_jit_access_juror_db_admin.object_id
  principal_name      = data.azuread_group.dts_jit_access_juror_db_admin.display_name
  principal_type      = "Group"

  depends_on = [module.postgresql_flexible_pitr]
}

import {
  for_each = {
    for name, id in local.postgres_pitr_admin_import_ids : name => id
    if name == "DTS Platform Operations PostgreSQL Admin Access"
  }
  to = module.postgresql_flexible_pitr[0].azurerm_postgresql_flexible_server_active_directory_administrator.pgsql_adadmin
  id = each.value.id
}

import {
  for_each = {
    for name, id in local.postgres_pitr_admin_import_ids : name => id
    if name == "DTS JIT Access Juror DB Admin"
  }
  to = azurerm_postgresql_flexible_server_active_directory_administrator.jit_pitr[0]
  id = each.value.id
}

import {
  for_each = {
    for name, id in local.postgres_pitr_admin_import_ids : name => id
    if name == "jenkins-${var.env}-mi"
  }
  to = module.postgresql_flexible_pitr[0].azurerm_postgresql_flexible_server_active_directory_administrator.pgsql_additional_principal_admin[each.value.object_id]
  id = each.value.id
}

import {
  for_each = {
    for name, id in local.postgres_pitr_admin_import_ids : name => id
    if name == "jenkins-ptl-mi"
  }
  to = module.postgresql_flexible_pitr[0].azurerm_postgresql_flexible_server_active_directory_administrator.pgsql_principal_admin[0]
  id = each.value.id
}
