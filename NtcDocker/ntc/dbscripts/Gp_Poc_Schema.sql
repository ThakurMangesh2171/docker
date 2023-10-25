CREATE TABLE error_message(
error_message_uuid VARCHAR(45) PRIMARY KEY,
--Response Rfp Primary Key
response_rfp_id VARCHAR(100) NOT NULL,
error_messages_json jsonb,
insert_ts bigint NOT NULL,
updated_ts bigint
);


CREATE TABLE application_data (
  application_data_uuid VARCHAR(45) PRIMARY KEY,
--  foreign key
  object_id VARCHAR(100) NOT NULL,
  type VARCHAR(100),
  sub_type VARCHAR(100),
  status VARCHAR(100),
  json_data JSONB,
  insert_ts bigint NOT NULL,
  updated_ts bigint
);

CREATE TABLE user_personalised_data (
  user_personalised_data_uuid VARCHAR(45) PRIMARY KEY,
  user_id VARCHAR(100) NOT NULL,
  json_data JSONB,
  insert_ts bigint NOT NULL,
  updated_ts bigint
);