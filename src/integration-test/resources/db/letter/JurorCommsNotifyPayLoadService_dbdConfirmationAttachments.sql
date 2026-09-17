DELETE FROM juror_mod.notify_template_field
WHERE id IN (540, 541, 542, 543, 544, 640, 641, 642, 643, 644)
   OR (
       template_id IN ('8a71632e-34a2-421e-9f17-100a419be7ba', '7093b2b4-2f71-478b-a179-92321a7f77a7')
       AND template_field IN (
           'COURT_MAP_URL',
           'EMAIL_ATTACHMENT_ALLOWANCES_URL',
           'EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL',
           'EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL',
           'EMAIL_ATTACHMENT_JURY_GUIDE_URL'
       )
   );

INSERT INTO juror_mod.notify_template_field
    (id, template_id, template_field, position_from, position_to, mapper_object)
VALUES
    (540, '8a71632e-34a2-421e-9f17-100a419be7ba', 'COURT_MAP_URL', NULL, NULL, 'COURT_MAP_URL'),
    (541, '8a71632e-34a2-421e-9f17-100a419be7ba', 'EMAIL_ATTACHMENT_ALLOWANCES_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_ALLOWANCES_URL'),
    (542, '8a71632e-34a2-421e-9f17-100a419be7ba', 'EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL'),
    (543, '8a71632e-34a2-421e-9f17-100a419be7ba', 'EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL'),
    (544, '8a71632e-34a2-421e-9f17-100a419be7ba', 'EMAIL_ATTACHMENT_JURY_GUIDE_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_JURY_GUIDE_URL'),
    (640, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'COURT_MAP_URL', NULL, NULL, 'COURT_MAP_URL'),
    (641, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'EMAIL_ATTACHMENT_ALLOWANCES_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_ALLOWANCES_URL'),
    (642, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_LOSS_OF_EARNINGS_URL'),
    (643, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_GUIDANCE_EMPLOYERS_URL'),
    (644, '7093b2b4-2f71-478b-a179-92321a7f77a7', 'EMAIL_ATTACHMENT_JURY_GUIDE_URL', NULL, NULL,
        'EMAIL_ATTACHMENT_JURY_GUIDE_URL');

INSERT INTO juror_mod.app_setting (setting, value) VALUES
    ('EMAIL_ATTACHMENT_DOC_ALLOWANCES_EN', '5223A_juror_allowances.pdf'),
    ('EMAIL_ATTACHMENT_DOC_ALLOWANCES_CY', '5223A_juror_allowances_CY.pdf'),
    ('EMAIL_ATTACHMENT_DOC_LOSS_OF_EARNINGS_EN', '5223D_certificate_of_loss_of_earnings.pdf'),
    ('EMAIL_ATTACHMENT_DOC_LOSS_OF_EARNINGS_CY', '5223D_certificate_of_loss_of_earnings_CY.pdf'),
    ('EMAIL_ATTACHMENT_DOC_GUIDANCE_EMPLOYERS_EN', '5223E_guidance_for_employers.pdf'),
    ('EMAIL_ATTACHMENT_DOC_GUIDANCE_EMPLOYERS_CY', '5223E_guidance_for_employers_CY.pdf'),
    ('EMAIL_ATTACHMENT_DOC_JURY_GUIDE_EN', '5222_your_guide_to_jury_service.pdf'),
    ('EMAIL_ATTACHMENT_DOC_JURY_GUIDE_CY', '5222_your_guide_to_jury_service_CY.pdf')
ON CONFLICT (setting) DO UPDATE
SET value = EXCLUDED.value;

INSERT INTO juror_mod.court_email_attachment (loc_code, file_name_en, file_name_cy) VALUES
    ('415', 'EX104_chester.pdf', NULL),
    ('411', 'EX104_cardiff.pdf', 'EX104_cardiff_CY.pdf')
ON CONFLICT (loc_code) DO UPDATE
SET file_name_en = EXCLUDED.file_name_en,
    file_name_cy = EXCLUDED.file_name_cy;

UPDATE juror_mod.juror_pool
SET next_date = current_date + 10
WHERE juror_number IN ('555555565', '555555567')
  AND is_active = true;
