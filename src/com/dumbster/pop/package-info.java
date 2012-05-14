package com.dumbster.pop;

/**
 * Provides POP3 access to the same <code>MailStore</code> managed by dumbster's SMTP server.
 * For details of the POP3 protocol, see https://tools.ietf.org/html/rfc1939 RFC 1939,
 * https://tools.ietf.org/html/rfc2449 RFC 2449, https://tools.ietf.org/html/rfc5034 RFC 5034.
 * Because dumbster is not intended to be a full-featured SMTP server, with real authentication
 * and multiple mailboxes, this POP service similarly does not conform fully to the POP standard.
 * Notable exceptions: messages, once deleted, are deleted; the server does not wait until the
 * normal QUIT command to really delete a message. The mailstore is not locked while the POP
 * session is active.
 *
 * Things that could be better include, but are not limited to: byte-stuffing (I would love if
 * someone could contribute a unit test to demonstrate the acceptable application of replacing "."
 * with "..") and advertising the pseudo-support of APOP.
 */
