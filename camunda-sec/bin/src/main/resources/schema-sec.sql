CREATE TABLE public.sagalog (
 trace_id text NOT NULL,
 state text NOT NULL,
 time_stamp timestamp NOT NULL,
 compensation bool NOT NULL,
 activity_id text NOT NULL,
 group_id text NOT NULL,
 result text NOT NULL,
 CONSTRAINT sagalog_pk PRIMARY KEY (trace_id, activity_id, state, result)
);