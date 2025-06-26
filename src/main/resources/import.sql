insert into ROLES(id, name, created_date, updated_date) values(nextval('roles_id_seq'), 'USER',current_timestamp,current_timestamp);
insert into ROLES(id, name, created_date, updated_date) values(nextval('roles_id_seq'), 'ADMIN', current_timestamp, current_timestamp);

insert into application_users(id,picture_url, version, username, enabled, hashed_password, first_name, last_name, email, created_date, updated_date) values (nextval('application_users_id_seq'),'https://avatar.iran.liara.run/public/job/operator/male', 0, 'admin', true,'$2a$10$TwLLzV1f0rIwFQ8uY2qtL.kXoEPhiz0dxC6dEXlVngi30G9.VNYiW', 'Admin', 'User', 'admin@example.com', current_timestamp, current_timestamp);

insert into user_roles(user_id, role_id) values(1, 1);
insert into user_roles(user_id, role_id) values(1, 2);
