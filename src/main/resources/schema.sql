DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name varchar(100) NOT NULL,
  email varchar(200) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description varchar(1500) NOT NULL,
    requestor_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT pk_request PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(1500) NOT NULL,
  available BOOLEAN,
  owner_id BIGINT REFERENCES users (id) ON DELETE CASCADE NOT NULL,
  request_id BIGINT REFERENCES requests (id) ON DELETE CASCADE,
  CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text varchar(1500) NOT NULL,
    item_id BIGINT REFERENCES items (id) ON DELETE CASCADE NOT NULL,
    author_id BIGINT REFERENCES users (id) ON DELETE CASCADE  NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id BIGINT REFERENCES items (id) ON DELETE CASCADE NOT NULL,
    booker_id BIGINT REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    status varchar(50),
    CONSTRAINT pk_booking PRIMARY KEY (id)
);


