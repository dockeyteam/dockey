import { gql } from 'graphql-request';

// Fragment for user fields
export const USER_FIELDS = gql`
  fragment UserFields on User {
    id
    username
    email
    firstName
    lastName
    role
    createdAt
  }
`;

// Query to get all users
export const GET_USERS = gql`
  ${USER_FIELDS}
  query GetUsers {
    users {
      ...UserFields
    }
  }
`;

// Query to get user by ID
export const GET_USER_BY_ID = gql`
  ${USER_FIELDS}
  query GetUserById($id: ID!) {
    userById(id: $id) {
      ...UserFields
    }
  }
`;
