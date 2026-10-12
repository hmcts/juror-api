locals {
  vault_name = "${var.product}-${var.env}"
  rg_name    = "${var.product}-${var.env}-rg"
  db_name    = "juror"
  db_port    = 5432

  postgres_pitr_enabled = var.postgres_pitr == null ? false : var.env == var.postgres_pitr.env
  postgres_pitr_cutover = local.postgres_pitr_enabled && var.postgres_use_pitr

  postgres_active_fqdn     = local.postgres_pitr_cutover ? module.postgresql_flexible_pitr[0].fqdn : module.postgresql_flexible.fqdn
  postgres_active_username = local.postgres_pitr_cutover ? module.postgresql_flexible_pitr[0].username : module.postgresql_flexible.username
  postgres_active_password = local.postgres_pitr_cutover ? module.postgresql_flexible_pitr[0].password : module.postgresql_flexible.password
}
