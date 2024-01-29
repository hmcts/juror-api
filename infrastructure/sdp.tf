module "sdp_db_user" {

  source = "git@github.com:hmcts/terraform-module-sdp-db-user?ref=master"

  server_name       = "${var.product}-${var.component}-${var.env}"
  server_fqdn       = module.postgresql_flexible.fqdn
  server_admin_user = module.postgresql_flexible.username
  server_admin_pass = module.postgresql_flexible.password

  count = var.env == "sandbox" || var.env == "dev" || var.env == "test" || var.env == "demo" ? 0 : 1

  databases = [
    {
      name : "juror"
    }
  ]

  database_schemas = {
    juror = ["juror_mod"]
  }

  common_tags = var.common_tags

  depends_on = [
    module.postgresql_flexible
  ]
}